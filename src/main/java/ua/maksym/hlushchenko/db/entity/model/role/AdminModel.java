package ua.maksym.hlushchenko.db.entity.model.role;

import lombok.Data;
import ua.maksym.hlushchenko.db.entity.role.Admin;

@Data
public class AdminModel extends UserModel implements Admin {}
