package com.asusoftware.only_feet_api.payment.service;

import com.asusoftware.only_feet_api.user.model.User;
import com.asusoftware.only_feet_api.user.repository.UserRepository;

import com.stripe.Stripe;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StripePaymentService {

    private final UserRepository userRepository;

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${frontend.success.url}")
    private String successUrl;

    @Value("${frontend.cancel.url}")
    private String cancelUrl;

    // Comisionul platformei în bani (ex: 200 = 2 RON, dacă moneda e RON)
    private static final Long PLATFORM_FEE_AMOUNT = 200L;

    public String createCheckoutSession(UUID userId, UUID creatorId) {
        Stripe.apiKey = stripeApiKey;

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creatorul nu există"));

        if (creator.getStripeAccountId() == null || creator.getStripePriceId() == null) {
            throw new RuntimeException("Creatorul nu are cont Stripe sau price ID setat.");
        }

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .addPaymentMethodType(SessionCreateParams.PaymentMethodType.CARD)
                .putMetadata("creatorId", creatorId.toString()) // ✅ aici e corect
                .setCustomerEmail(userRepository.findById(userId)
                        .orElseThrow(() -> new RuntimeException("Userul nu există"))
                        .getEmail())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPrice(creator.getStripePriceId())
                                .build()
                )
                .setSubscriptionData(
                        SessionCreateParams.SubscriptionData.builder()
                                .setTransferData(
                                        SessionCreateParams.SubscriptionData.TransferData.builder()
                                                .setDestination(creator.getStripeAccountId())
                                                .build()
                                )
                                .build()
                )
                .setPaymentIntentData(
                        SessionCreateParams.PaymentIntentData.builder()
                                .setApplicationFeeAmount(PLATFORM_FEE_AMOUNT)
                                .build()
                )
                .build();



        try {
            Session session = Session.create(params);
            return session.getUrl();
        } catch (Exception e) {
            throw new RuntimeException("Eroare la crearea sesiunii Stripe: " + e.getMessage());
        }
    }

}
