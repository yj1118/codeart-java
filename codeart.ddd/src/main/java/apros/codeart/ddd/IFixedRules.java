package apros.codeart.ddd;

/**
 * 固定规则，每个领域对象都会有一组固定规则
 */
public interface IFixedRules {
	ValidationResult validate(IDomainObject obj);
}
