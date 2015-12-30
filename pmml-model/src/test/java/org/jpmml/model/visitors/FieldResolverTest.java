/*
 * Copyright (c) 2015 Villu Ruusmann
 */
package org.jpmml.model.visitors;

import java.io.InputStream;
import java.util.Collections;
import java.util.Set;

import javax.xml.transform.stream.StreamSource;

import org.dmg.pmml.Apply;
import org.dmg.pmml.DerivedField;
import org.dmg.pmml.Field;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.PMML;
import org.dmg.pmml.RegressionTable;
import org.dmg.pmml.Segment;
import org.dmg.pmml.VisitorAction;
import org.jpmml.model.FieldNameUtil;
import org.jpmml.model.FieldUtil;
import org.jpmml.model.JAXBUtil;
import org.jpmml.model.PMMLUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FieldResolverTest {

	@Test
	public void resolve() throws Exception {
		PMML pmml;

		try(InputStream is = PMMLUtil.getResourceAsStream(FieldResolverTest.class)){
			pmml = JAXBUtil.unmarshalPMML(new StreamSource(is));
		}

		final
		Set<FieldName> dataFieldNames = FieldNameUtil.create("y", "x1", "x2", "x3");

		final
		Set<FieldName> pmmlNames = FieldNameUtil.create(dataFieldNames, "x1_squared", "x1_cubed");

		FieldResolver applyResolver = new FieldResolver(){

			@Override
			public VisitorAction visit(Apply apply){
				Set<Field> fields = getFields();

				String function = apply.getFunction();

				if("*".equals(function)){
					DerivedField derivedField = (DerivedField)VisitorUtil.getParent(this);

					FieldName name = derivedField.getName();

					if("x1_squared".equals(name.getValue())){
						checkFields(dataFieldNames, fields);
					} else

					if("x1_cubed".equals(name.getValue())){
						checkFields(FieldNameUtil.create(dataFieldNames, "x1_squared"), fields);
					} else

					{
						throw new AssertionError();
					}
				} else

				if("pow".equals(function)){
					checkFields(FieldNameUtil.create("x"), fields);
				} else

				if("square".equals(function)){
					checkFields(FieldNameUtil.create(pmmlNames, "first_output"), fields);
				} else

				if("cube".equals(function)){
					checkFields(FieldNameUtil.create(pmmlNames, "first_output", "x2_squared"), fields);
				} else

				{
					throw new AssertionError();
				}

				return super.visit(apply);
			}
		};

		applyResolver.applyTo(pmml);

		assertEquals(Collections.emptySet(), applyResolver.getFields());

		FieldResolver regressionTableResolver = new FieldResolver(){

			@Override
			public VisitorAction visit(RegressionTable regressionTable){
				Set<Field> fields = getFields();

				Segment segment = (Segment)VisitorUtil.getParent(this, 1);

				String id = segment.getId();

				if("first".equals(id)){
					checkFields(pmmlNames, fields);
				} else

				if("second".equals(id)){
					checkFields(FieldNameUtil.create(pmmlNames, "first_output", "x2_squared", "x2_cubed"), fields);
				} else

				if("third".equals(id)){
					checkFields(FieldNameUtil.create(pmmlNames, "first_output", "second_output"), fields);
				} else

				if("sum".equals(id)){
					checkFields(FieldNameUtil.create(pmmlNames, "first_output", "second_output", "third_output"), fields);
				} else

				{
					throw new AssertionError();
				}

				return super.visit(regressionTable);
			}
		};

		regressionTableResolver.applyTo(pmml);

		assertEquals(Collections.emptySet(), regressionTableResolver.getFields());
	}

	static
	private void checkFields(Set<FieldName> names, Set<Field> fields){
		assertEquals(names, FieldUtil.nameSet(fields));
	}
}