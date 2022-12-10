package ua.maksym.hlushchenko.db.entity.role;

import ua.maksym.hlushchenko.db.entity.Receipt;
import ua.maksym.hlushchenko.db.entity.Subscription;

import java.util.Set;

public interface Reader extends User {
    boolean isBlocked();
    void setBlocked(boolean state);
    Set<Receipt> getReceipts();
    void setReceipts(Set<Receipt> receipts);
    Set<Subscription> getSubscriptions();
    void setSubscriptions(Set<Subscription> subscriptions);
}
