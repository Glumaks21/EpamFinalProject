package ua.maksym.hlushchenko.dao.entity.role;

import lombok.*;
import ua.maksym.hlushchenko.dao.entity.Receipt;
import ua.maksym.hlushchenko.dao.entity.Subscription;
import ua.maksym.hlushchenko.orm.entity.annotations.*;

import java.util.*;

@Getter
@Setter
@ToString(callSuper = true, exclude = {"receipts", "subscriptions"})
@EqualsAndHashCode(callSuper = true)
@Entity
@Table("reader")
@IdMappedByColumn("user_id")
public class Reader extends AbstractUser {
    @Column("blocked")
    private boolean blocked;

    @OneToMany(genericType = Receipt.class,
            cascadeTypes = {CascadeType.ALL},
            mappedBy = "reader")
    private List<Receipt> receipts;

    @OneToMany(genericType = Subscription.class,
            cascadeTypes = {CascadeType.ALL},
            mappedBy = "reader")
    private List<Subscription> subscriptions;
}
