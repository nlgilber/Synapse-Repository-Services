package org.sagebionetworks.table.query.model;

import java.util.List;

public class Factor extends SQLElement {

	private Sign sign;
	private NumericPrimary numericPrimary;

	public Factor(Sign sign, NumericPrimary numericPrimary) {
		this.sign = sign;
		this.numericPrimary = numericPrimary;
	}

	public NumericPrimary getNumericPrimary() {
		return numericPrimary;
	}

	@Override
	public void toSql(StringBuilder builder) {
		if(sign != null){
			builder.append(sign.toSQL());
		}
		numericPrimary.toSql(builder);
	}

	@Override
	<T extends Element> void addElements(List<T> elements, Class<T> type) {
		checkElement(elements, type, numericPrimary);
	}
}
