package subsystem.account;

import apros.codeart.ddd.QueryLevel;
import apros.codeart.ddd.repository.access.DataPortal;
import apros.codeart.ddd.repository.access.SqlRepository;
import apros.codeart.util.SafeAccess;

@SafeAccess
public class AuthPlatformRepository extends SqlRepository<AuthPlatform> implements IAuthPlatformRepository {

    @Override
    public Iterable<AuthPlatform> finds() {
        return null;
    }

    @Override
    public AuthPlatform findByEN(String en, QueryLevel level) {

        return DataPortal.querySingle(AuthPlatform.class, "EN=@EN", (arg) ->
        {
            arg.put("EN", en);
        }, level);

    }

    @Override
    public AuthPlatform findByName(String name, QueryLevel level) {
        return DataPortal.querySingle(AuthPlatform.class, "name like %@name%", (arg) ->
        {
            arg.put("name", name);
        }, level);
    }
}
