package com.ucop.desktop.service;

import com.ucop.desktop.domain.Account;
import com.ucop.desktop.domain.Promotion;
import com.ucop.desktop.repo.PromotionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PromotionService {
    private final PromotionRepository promotions;

    public PromotionService(PromotionRepository promotions) {
        this.promotions = promotions;
    }

    @Transactional
    public Promotion save(Account actor, Promotion promotion) {
        if (actor == null || actor.getRole() == null
                || !"ADMIN".equalsIgnoreCase(actor.getRole().getRoleName())) {
            throw new SecurityException("Chỉ Admin được quản lý khuyến mãi.");
        }
        if (promotion.getCode() == null || promotion.getCode().isBlank()
                || promotion.getValue() == null || promotion.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Mã và giá trị khuyến mãi không hợp lệ.");
        }
        if (!"PERCENTAGE".equalsIgnoreCase(promotion.getDiscountType())
                && !"FIXED_AMOUNT".equalsIgnoreCase(promotion.getDiscountType())) {
            throw new IllegalArgumentException("Loại giảm phải là PERCENTAGE hoặc FIXED_AMOUNT.");
        }
        if ("PERCENTAGE".equalsIgnoreCase(promotion.getDiscountType())
                && promotion.getValue().compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Phần trăm giảm không được vượt quá 100.");
        }
        promotion.setCode(promotion.getCode().trim().toUpperCase());
        Promotion existing = promotions.findByCodeIgnoreCase(promotion.getCode()).orElse(null);
        if (existing != null && !existing.getPromotionId().equals(promotion.getPromotionId())) {
            existing.setDiscountType(promotion.getDiscountType());
            existing.setValue(promotion.getValue());
            existing.setExpiryDate(promotion.getExpiryDate());
            return promotions.save(existing);
        }
        return promotions.save(promotion);
    }
}
