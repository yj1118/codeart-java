package subsystem.account;

import apros.codeart.ddd.IRepository;
import apros.codeart.ddd.QueryLevel;

public interface IAuthPlatformRepository extends IRepository<AuthPlatform> {
    Iterable<AuthPlatform> finds();

    AuthPlatform findByEN(String en, QueryLevel level);
}
