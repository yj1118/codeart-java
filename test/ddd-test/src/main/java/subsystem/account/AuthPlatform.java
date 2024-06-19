package subsystem.account;

import apros.codeart.ddd.*;
import apros.codeart.ddd.repository.ConstructorRepository;
import apros.codeart.ddd.repository.PropertyRepository;
import apros.codeart.ddd.validation.NotEmpty;
import apros.codeart.ddd.validation.StringLength;


@ObjectValidator(AuthPlatformSpecification.class)
public class AuthPlatform extends AggregateRootLong {

    //region 基础代码

    //region 名称

    @PropertyRepository
    @NotEmpty
    @StringLength(min = 1, max = 20)
    public static final DomainProperty NameProperty = DomainProperty.register("Name", String.class, AuthPlatform.class);

    public String name() {
        return this.getValue(NameProperty, String.class);
    }

    public void name(String value) {
        this.setValue(NameProperty, value);
    }

    //endregion

    //region 英文名称


    @PropertyRepository
    @NotEmpty
    @StringLength(min = 1, max = 50)
    public static final DomainProperty ENProperty = DomainProperty.register("EN", String.class, AuthPlatform.class);

    /**
     * 英文名
     */
    public String en() {
        return this.getValue(ENProperty, String.class);
    }

    public void en(String value) {
        this.setValue(ENProperty, value);
    }

    //endregion

    //region 描述

    @PropertyRepository
    @StringLength(max = 100)
    public static final DomainProperty DescriptionProperty = DomainProperty.register("Description", String.class, AuthPlatform.class);

    /**
     * 100字以内的描述，可以为空
     */
    public String description() {
        return this.getValue(DescriptionProperty, String.class);
    }

    public void description(String value) {
        this.setValue(DescriptionProperty, value);
    }

    //endregion

    //region 序号，供客户端排序展示用


    @PropertyRepository
    static final DomainProperty OrderIndexProperty = DomainProperty.register("OrderIndex", int.class, AuthPlatform.class, (o, dp) -> 1);


    /**
     * 序号
     *
     * @return 序号
     */
    public int orderIndex() {
        return this.getValue(OrderIndexProperty, int.class);
    }

    public void orderIndex(int value) {
        this.setValue(OrderIndexProperty, value);
    }

    //endregion


    //region 是否为系统平台，系统平台不能被删除

    @PropertyRepository
    private static final DomainProperty IsSystemProperty = DomainProperty.register("IsSystem", boolean.class, AuthPlatform.class);

    /**
     * @return 是否为系统平台，系统平台不能被删除
     */
    public boolean isSystem() {
        return this.getValue(IsSystemProperty, boolean.class);
    }

    private void isSystem(boolean value) {
        this.setValue(IsSystemProperty, value);
    }

    //endregion


    @ConstructorRepository
    public AuthPlatform(long id, boolean isSystem) {
        super(id);
        this.isSystem(isSystem);
        this.onConstructed();
    }

    private static class AuthPlatformEmpty extends AuthPlatform {
        public AuthPlatformEmpty() {
            super(0L, false);
            this.onConstructed();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        public static final AuthPlatformEmpty INSTANCE = new AuthPlatformEmpty();

    }

    public static AuthPlatform empty() {
        return AuthPlatformEmpty.INSTANCE;
    }


    //endregion

    public AuthPlatform(long id, String name, String en) {
        super(id);
        this.name(name);
        this.en(en);
        this.isSystem(false);
        this.onConstructed();
    }


}
