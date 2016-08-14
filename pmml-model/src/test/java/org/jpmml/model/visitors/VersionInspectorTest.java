/*
 * Copyright (c) 2014 Villu Ruusmann
 */
package org.jpmml.model.visitors;

import java.util.Arrays;

import org.dmg.pmml.Apply;
import org.dmg.pmml.DataDictionary;
import org.dmg.pmml.DataType;
import org.dmg.pmml.DefineFunction;
import org.dmg.pmml.FieldName;
import org.dmg.pmml.Header;
import org.dmg.pmml.OpType;
import org.dmg.pmml.Output;
import org.dmg.pmml.OutputField;
import org.dmg.pmml.PMML;
import org.dmg.pmml.PMMLObject;
import org.dmg.pmml.ParameterField;
import org.dmg.pmml.SequenceModel;
import org.dmg.pmml.Target;
import org.dmg.pmml.TargetValue;
import org.dmg.pmml.Targets;
import org.dmg.pmml.TextModel;
import org.dmg.pmml.TimeSeriesModel;
import org.dmg.pmml.TransformationDictionary;
import org.dmg.pmml.TreeModel;
import org.dmg.pmml.association.AssociationModel;
import org.dmg.pmml.baseline.BaselineModel;
import org.dmg.pmml.bayesian_network.BayesianNetworkModel;
import org.dmg.pmml.gaussian_process.GaussianProcessModel;
import org.dmg.pmml.general_regression.GeneralRegressionModel;
import org.dmg.pmml.general_regression.PPCell;
import org.dmg.pmml.general_regression.PPMatrix;
import org.dmg.pmml.naive_bayes.NaiveBayesModel;
import org.dmg.pmml.nearest_neighbor.NearestNeighborModel;
import org.dmg.pmml.neural_network.NeuralNetwork;
import org.dmg.pmml.regression.RegressionModel;
import org.dmg.pmml.rule_set.RuleSetModel;
import org.dmg.pmml.scorecard.Scorecard;
import org.jpmml.schema.Version;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class VersionInspectorTest {

	@Test
	public void inspectTypeAnnotations(){
		PMML pmml = createPMML();

		assertVersionRange(pmml, Version.PMML_3_0, Version.PMML_4_3);

		pmml.addModels(new AssociationModel(),
			//new ClusteringModel(),
			//new GeneralRegressionModel(),
			//new MiningModel(),
			new NaiveBayesModel(),
			new NeuralNetwork(),
			new RegressionModel(),
			new RuleSetModel(),
			new SequenceModel(),
			//new SupportVectorMachineModel(),
			new TextModel(),
			new TreeModel());

		assertVersionRange(pmml, Version.PMML_3_0, Version.PMML_4_3);

		pmml.addModels(new TimeSeriesModel());

		assertVersionRange(pmml, Version.PMML_4_0, Version.PMML_4_3);

		pmml.addModels(new BaselineModel(),
			new Scorecard(),
			new NearestNeighborModel());

		assertVersionRange(pmml, Version.PMML_4_1, Version.PMML_4_3);

		pmml.addModels(new BayesianNetworkModel(),
			new GaussianProcessModel());

		assertVersionRange(pmml, Version.PMML_4_3, Version.PMML_4_3);
	}

	@Test
	public void inspectFieldAnnotations(){
		PMML pmml = createPMML();

		AssociationModel model = new AssociationModel();

		pmml.addModels(model);

		assertVersionRange(pmml, Version.PMML_3_0, Version.PMML_4_3);

		Output output = new Output();

		model.setOutput(output);

		assertVersionRange(pmml, Version.PMML_4_0, Version.PMML_4_3);

		model.setScorable(Boolean.FALSE);

		assertVersionRange(pmml, Version.PMML_4_1, Version.PMML_4_3);

		model.setScorable(null);

		assertVersionRange(pmml, Version.PMML_4_0, Version.PMML_4_3);

		OutputField outputField = new OutputField()
			.setRuleFeature(OutputField.RuleFeatureType.AFFINITY);

		output.addOutputFields(outputField);

		assertVersionRange(pmml, Version.PMML_4_1, Version.PMML_4_2);

		outputField.setDataType(DataType.DOUBLE);

		assertVersionRange(pmml, Version.PMML_4_1, Version.PMML_4_3);

		model.setOutput(null);

		assertVersionRange(pmml, Version.PMML_3_0, Version.PMML_4_3);
	}

	@Test
	public void inspectValueAnnotations(){
		PMML pmml = createPMML();

		FieldName name = FieldName.create("y");

		Target target = new Target()
			.setField(name)
			.addTargetValues(createTargetValue("no event"), createTargetValue("event"));

		Targets targets = new Targets()
			.addTargets(target);

		GeneralRegressionModel model = new GeneralRegressionModel()
			.setTargets(targets);

		pmml.addModels(model);

		assertVersionRange(pmml, Version.PMML_3_0, Version.PMML_3_0);

		PPMatrix ppMatrix = new PPMatrix()
			.addPPCells(new PPCell(), new PPCell());

		model.setPPMatrix(ppMatrix);

		assertVersionRange(pmml, Version.PMML_3_0, Version.PMML_4_3);

		target.setField(null);

		assertVersionRange(pmml, Version.PMML_4_3, Version.PMML_4_3);
	}

	@Test
	public void inspectFunctions(){
		PMML pmml = createPMML();

		assertVersionRange(pmml, Version.PMML_3_0, Version.PMML_4_3);

		Apply apply = new Apply()
			.setFunction("lowercase");

		DefineFunction defineFunction = new DefineFunction("convert_case", OpType.CATEGORICAL, null)
			.addParameterFields(new ParameterField(FieldName.create("string")))
			.setExpression(apply);

		TransformationDictionary transformationDictionary = new TransformationDictionary()
			.addDefineFunctions(defineFunction);

		pmml.setTransformationDictionary(transformationDictionary);

		assertVersionRange(pmml, Version.PMML_4_1, Version.PMML_4_3);

		apply.setFunction("uppercase");

		assertVersionRange(pmml, Version.PMML_3_0, Version.PMML_4_3);

		apply.setFunction(null);

		assertVersionRange(pmml, Version.PMML_3_0, Version.PMML_3_0);
	}

	static
	private PMML createPMML(){
		Header header = new Header()
			.setCopyright("ACME Corporation");

		DataDictionary dataDictionary = new DataDictionary();

		PMML pmml = new PMML("4.3", header, dataDictionary);

		return pmml;
	}

	static
	private TargetValue createTargetValue(String value){
		TargetValue targetValue = new TargetValue()
			.setValue(value);

		return targetValue;
	}

	static
	private void assertVersionRange(PMMLObject object, Version minimum, Version maximum){
		VersionInspector inspector = new VersionInspector();
		inspector.applyTo(object);

		assertEquals(Arrays.asList(minimum, maximum), Arrays.asList(inspector.getMinimum(), inspector.getMaximum()));
	}
}
