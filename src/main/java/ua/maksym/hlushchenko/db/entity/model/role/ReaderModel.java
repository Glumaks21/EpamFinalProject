package ua.maksym.hlushchenko.db.entity.model.role;

import lombok.Data;
import ua.maksym.hlushchenko.db.entity.*;
import ua.maksym.hlushchenko.db.entity.role.Reader;

import java.util.Set;

@Data
public class ReaderModel extends UserModel implements Reader {
    private boolean isBlocked;

    Set<Receipt> receipts;
    Set<Subscription> subscriptions;
}
