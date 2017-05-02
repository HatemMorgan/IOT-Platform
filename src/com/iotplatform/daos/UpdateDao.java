package com.iotplatform.daos;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Hashtable;

import org.apache.jena.update.UpdateAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iotplatform.exceptions.DatabaseException;
import com.iotplatform.ontology.Class;
import com.iotplatform.queries.UpdateQuery;
import com.iotplatform.utilities.InsertionPropertyValue;
import com.iotplatform.utilities.UpdatePropertyValueUtility;
import com.iotplatform.utilities.UpdateRequestValidationResultUtility;

import oracle.spatial.rdf.client.jena.DatasetGraphOracleSem;
import oracle.spatial.rdf.client.jena.GraphOracleSem;
import oracle.spatial.rdf.client.jena.Oracle;

@Repository("updateDao")
public class UpdateDao {

	private Oracle oracle;

	@Autowired
	public UpdateDao(Oracle oracle) {
		this.oracle = oracle;
	}

	/**
	 * updateData is used to call UpdateQuery class to construct query and it
	 * executes the query.
	 * 
	 * updateData also checks if the updateRequest want to update the value of
	 * an uniqueIdentifer so It will call
	 * constructUpdateQueryToUpdateUniqueIdentifierValue method in UpdateQuery
	 * class to update the uniqueIdentfier only. Then call constructUpdateQuery
	 * method in UpdateQuery class to update existing values and insert new data
	 * 
	 * @param applicationModelName
	 *            The requested application modelName in the database
	 * 
	 * @param subjectClass
	 *            The requested subjectClass
	 * 
	 * @param uniqueIdentifier
	 *            uniqueIdentifier of the individual that needs to be updated
	 * 
	 * @param updatePropertyValueList
	 *            is the parsed result of the validation method in
	 *            UpdateRequestValidation class
	 * 
	 * @param htblClassInsertionPropertyValue
	 *            is the parsed result of the validation method in
	 *            InsertRequestValidation class
	 */
	public void updateData(String applicationModelName, Class subjectClass, String uniqueIdentifier,
			UpdateRequestValidationResultUtility updateRequestValidationResult,
			Hashtable<String, ArrayList<ArrayList<InsertionPropertyValue>>> htblClassInsertionPropertyValue) {

		try {
			/*
			 * check if the update request will update the uniqueIdentfier by
			 * checking updateRequestValidationResult;s boolean flag
			 * isRequestUpdatesUniqueIdentifierValue
			 */
			if (updateRequestValidationResult != null
					&& updateRequestValidationResult.isRequestUpdatesUniqueIdentifierValue()) {
				/*
				 * get newUniqueIdentifierValue which is stored in
				 * updateRequestValidationResult
				 */
				String newUniqueIdentifierValue = updateRequestValidationResult.getNewUniqueIdentifierValue()
						.toString();

				/*
				 * call constructUpdateQueryToUpdateUniqueIdentifierValue method
				 * in UpdateQuery class to construct update query to update the
				 * uniqueIdentifier
				 */
				String updateUniqueIdentifierQuery = UpdateQuery.constructUpdateQueryToUpdateUniqueIdentifierValue(
						subjectClass, newUniqueIdentifierValue, uniqueIdentifier, applicationModelName);

				uniqueIdentifier = newUniqueIdentifierValue;

				System.out.println(updateUniqueIdentifierQuery);

				GraphOracleSem graphOracleSem = new GraphOracleSem(oracle, applicationModelName);
				DatasetGraphOracleSem dsgos = DatasetGraphOracleSem.createFrom(graphOracleSem);

				UpdateAction.parseExecute(updateUniqueIdentifierQuery, dsgos);
				dsgos.close();

			}

			String updateQuery;
			if (updateRequestValidationResult != null) {
				updateQuery = UpdateQuery.constructUpdateQuery(subjectClass, uniqueIdentifier,
						updateRequestValidationResult.getValidationResult(), htblClassInsertionPropertyValue,
						applicationModelName);
			} else {
				updateQuery = UpdateQuery.constructUpdateQuery(subjectClass, uniqueIdentifier, null,
						htblClassInsertionPropertyValue, applicationModelName);
			}

			System.out.println(updateQuery);

			GraphOracleSem graphOracleSem = new GraphOracleSem(oracle, applicationModelName);
			DatasetGraphOracleSem dsgos = DatasetGraphOracleSem.createFrom(graphOracleSem);

			UpdateAction.parseExecute(updateQuery, dsgos);
			dsgos.close();

		} catch (SQLException ex) {
			throw new DatabaseException(ex.getMessage(), "Update API");
		}

	}

}
