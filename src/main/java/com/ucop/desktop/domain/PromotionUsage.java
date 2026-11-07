package com.ucop.desktop.domain;

import javax.persistence.*;

@Entity
@Table(name = "PROMOTION_USAGE")
public class PromotionUsage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer usageId;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "PromotionID")
    private Promotion promotion;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "AccountID")
    private Account account;
    @ManyToOne(optional = false, fetch = FetchType.LAZY) @JoinColumn(name = "OrderID")
    private CustomerOrder order;
    public void setPromotion(Promotion promotion) { this.promotion = promotion; }
    public void setAccount(Account account) { this.account = account; }
    public void setOrder(CustomerOrder order) { this.order = order; }
}
