package ua.maksym.hlushchenko.dao.entity.impl.role;

import lombok.*;
import ua.maksym.hlushchenko.orm.annotations.*;
import ua.maksym.hlushchenko.dao.entity.impl.*;

import java.util.*;

@Getter
@Setter
@ToString(callSuper = true, exclude = {"receipts", "subscriptions"})
@EqualsAndHashCode(callSuper = true)
@Table("reader")
@IdMappedByColumn("user_id")
public class Reader extends AbstractUser {
    @Column("blocked")
    private boolean blocked;

    @OneToMany(genericType = Receipt.class,
            cascadeType = {CascadeType.SAVE, CascadeType.UPDATE},
            mappedBy = "reader")
    private List<Receipt> receipts;

    @OneToMany(genericType = Subscription.class,
            cascadeType = {CascadeType.SAVE, CascadeType.UPDATE},
            mappedBy = "reader")
    private List<Subscription> subscriptions;
}
