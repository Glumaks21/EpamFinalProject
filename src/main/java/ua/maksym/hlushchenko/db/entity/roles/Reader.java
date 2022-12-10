package ua.maksym.hlushchenko.db.entity.roles;

import lombok.Data;
import ua.maksym.hlushchenko.db.entity.Receipt;
import ua.maksym.hlushchenko.db.entity.Subscription;

import java.util.Set;

@Data
public class Reader {
    private User user;
    private boolean isBlocked;

    Set<Receipt> receipts;
    Set<Subscription> subscriptions;
}
