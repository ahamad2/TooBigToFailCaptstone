package com.tbf;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBReader {
	/**
	 * @retrieveAddress
	 * finds the Address for a person based on the address Id.
	 * 
	 */
	public static Address retrieveAddress(int addressId) {
		Address address = new Address();
		Connection conn = DBTool.connectToDB();
		String query = "select * from Address where addressId = ?;";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			/**
			 * The address finds the needed country and state for the address by using the 
			 * @retrieveCountry and @retrieveState methods which are passed the country Id and state Id respectively. 
			 */
			ps = conn.prepareStatement(query);
			ps.setInt(1, addressId);  //Setting the '?' variables in the query to the target variable.
			rs = ps.executeQuery();
			rs.next();
			address = new Address(rs.getString("street"), rs.getString("city"), retrieveState(rs.getInt("stateId")), rs.getString("zip"), retrieveCountry(rs.getInt("countryId")));
			
		} catch (SQLException sqle) {
			throw new RuntimeException(sqle);
		}
		return address;
	}
	
	/**
	 * @retrieveState
	 * returns the State of the address as a string.
	 * Finds the state using the state Id.
	 */
	public static String retrieveState(int stateId) {
		String state = null;
		Connection conn = DBTool.connectToDB();
		String query = "select abbreviation from State where stateId = ?;";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, stateId);  //Setting the '?' variables in the query to the target variable.
			rs = ps.executeQuery();
			rs.next();
			state = rs.getString("abbreviation");
		} catch(SQLException sqle) {
			throw new RuntimeException(sqle);
		}
		return state;
	}
	/**
	 * @retrieveCountry
	 * Finds the country for an address based on the country Id. 
	 * Returns the country as a string. 
	 */
	public static String retrieveCountry(int countryId) {
		String country = null;
		Connection conn = DBTool.connectToDB();
		String query = "select abbreviation from Country where countryId = ?;";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, countryId);  //Setting the '?' variables in the query to the target variable.
			rs = ps.executeQuery();
			rs.next();
			country = rs.getString("abbreviation");
		} catch(SQLException sqle) {
			throw new RuntimeException(sqle);
		}
		return country;
	}
	/**
	 * @retrieveEmailAddress
	 * finds the email address(s) of a person based on their Person Id. 
	 * returns a list that contains the email address(s).
	 */
	public static ArrayList<String> retrieveEmailAddress(int personId){
		ArrayList<String> emails = new ArrayList<>();
		Connection conn = DBTool.connectToDB();
		String query = "select emailAddress from EmailAddress where personId = ?;";
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, personId);  //Setting the '?' variables in the query to the target variable.
			rs = ps.executeQuery();
			
			while (rs.next()) {
				emails.add(rs.getString("emailAddress"));
			}
			
		} catch (SQLException sqle) {
			throw new RuntimeException(sqle);
		}
		DBTool.disconnectFromDB(conn, ps, rs);
		return emails;
	}
	/**
	 * @retrievePerson 
	 * Finds a person in the database based on their person Id. 
	 * Returns a Person object. 
	 */
	public static Person retrievePerson(int personId){
		if (personId == 0) {
			return null;
		}
		Person person = new Person();
		Connection conn = DBTool.connectToDB();
		String query = "select * from Person where personId = ?;";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, personId);  //Setting the '?' variables in the query to the target variable.
			rs = ps.executeQuery();
			rs.next();
			Name n = new Name(rs.getString("firstName"), rs.getString("lastName"));
			String brokerStat = rs.getString("brokerStat");
			if (brokerStat != null) {
				person = new Person(rs.getString("alphaCode"), brokerStat, n, retrieveAddress(rs.getInt("addressId")), 
						retrieveEmailAddress(rs.getInt("personId")));
			} else {
				person = new Person(rs.getString("alphaCode"), "", n, retrieveAddress(rs.getInt("addressId")), 
						retrieveEmailAddress(rs.getInt("personId")));
			
			}
		}catch (SQLException sqle) {
			throw new RuntimeException(sqle);
		}
		DBTool.disconnectFromDB(conn, ps, rs);
		return person;
	}
	
	/*
	 * @retrieveAllPerson 
	 * The method returns a list of Persons. 
	 * brings all Person records from the DB. 
	 */
	public static List<Person> retrieveAllPerson() {
		Connection conn = DBTool.connectToDB();
		String query = "select * from Person;";
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		List<Person> persons = new ArrayList<>();
		
			try {
				ps = conn.prepareStatement(query);
				rs = ps.executeQuery();
				while (rs.next()) {
					/**
					 * While iterating through the loop, @retrieveAddress and @retrieveEmailAddress methods are used to bring in the 
					 * appropriate data and pass them into the Person object.
					 * @retrieveAddress is passed the address Id and returns an Address object.
					 * @retrieveEmailAddress is passed the person Id and returns the email address(s). 
					 */
					Name n = new Name(rs.getString("firstName"), rs.getString("lastName"));
					Person p = new Person(rs.getString("alphaCode"), rs.getString("brokerStat"), n, retrieveAddress(rs.getInt("addressId")), 
							retrieveEmailAddress(rs.getInt("personId")));
					persons.add(p);
				}
			} catch (SQLException sqle) {
				throw new RuntimeException(sqle);
			}
			
			DBTool.disconnectFromDB(conn, ps, rs);
			return persons;
	}
	/**
	 * @retireveAllAssets
	 * The method brings in all the assets found in the DB and passes the correct asset accordingly to each object based on the asset type.
	 * The method returns a list of Assets.
	 */
	public static List<Asset> retrieveAllAssets() {
		List<Asset> assets = new ArrayList<>();
		Connection conn = DBTool.connectToDB();
		String query = "select * from Asset;";
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			
			while (rs.next()) {
				/**
				 * The asset type is checked and then the appropriate information is passed into a Deposit, Stock or Private Investment obejct. 
				 */
				if (rs.getString("assetType").equals("D")) {
					Asset newDepo  = new DepositAsset(rs.getString("assetCode"), rs.getString("assetType"), rs.getString("label"), rs.getDouble("apr"));
					assets.add(newDepo);

				} else if (rs.getString("assetType").equals("S")) {
					Asset newStock = new Stock(rs.getString("assetCode"), rs.getString("assetType"), rs.getString("label"), rs.getDouble("quartDivi"), rs.getDouble("baseROR"),
							rs.getDouble("beta"), rs.getString("stockSymb"), rs.getDouble("SharePrice"));
					assets.add(newStock);

					
				} else if (rs.getString("assetType").equals("P")) {
					Asset newPI = new PrivateInvest(rs.getString("assetCode"), rs.getString("assetType"), rs.getString("label"), rs.getDouble("quartDivi"), 
							rs.getDouble("baseROR"), rs.getDouble("omega"), rs.getDouble("investmentValue"));
					assets.add(newPI);

				}
 			}
		} catch (SQLException sqle) {
			throw new RuntimeException(sqle);
		}
		
		DBTool.disconnectFromDB(conn, ps, rs);
		return assets;
	}
	/**
	 * @retrieveAssets 
	 * A method that is passed a portoflio Id and returns a List of assets for that portfolio. 
	 * The query used joins the PortfolioAsset and Asset table in the database to display the relevant information. 
	 */
	public static List<Asset> retrieveAssets(int portId) {
		List<Asset> assets = new ArrayList<>();
		Connection conn = DBTool.connectToDB();
		String query = "select * from Asset a join PortfolioAsset pa on a.assetId = pa.assetId where pa.portId = ?;";
		
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		try {
			ps = conn.prepareStatement(query);
			ps.setInt(1, portId); //Setting the '?' variables in the query to the target variable. 
			rs = ps.executeQuery();
			
			while (rs.next()) {
				/**
				 * Before passing the variables needed into each constructor, we check the asset type to pass in the correct variables to the correct object.
				 */
				if (rs.getString("assetType").equals("D")) {
					Asset newDepo  = new DepositAsset(rs.getString("assetCode"), rs.getString("assetType"), rs.getString("label"), rs.getDouble("apr"), 
							rs.getDouble("assetInfo"));
					assets.add(newDepo);

				} else if (rs.getString("assetType").equals("S")) {
					Asset newStock = new Stock(rs.getString("assetCode"), rs.getString("assetType"), rs.getString("label"), rs.getDouble("quartDivi"), rs.getDouble("baseROR"),
							rs.getDouble("beta"), rs.getString("stockSymb"), rs.getDouble("SharePrice"), rs.getDouble("assetInfo"));
					assets.add(newStock);

					
				} else if (rs.getString("assetType").equals("P")) {
					Asset newPI = new PrivateInvest(rs.getString("assetCode"), rs.getString("assetType"), rs.getString("label"), rs.getDouble("quartDivi"), 
							rs.getDouble("baseROR"), rs.getDouble("omega"), rs.getDouble("investmentValue"), rs.getDouble("assetInfo"));
					assets.add(newPI);

				}
 			}
		} catch (SQLException sqle) {
			throw new RuntimeException(sqle);
		}
		
		DBTool.disconnectFromDB(conn, ps, rs);
		return assets;
	}
	/**
	 * @retrieveAllPortfolios
	 * The method retrieves all portfolios from the DB.
	 * This is done by connection to the DB, setting an sql query that retrieves all portfolios, then passing the result set 
	 * information into each constructor.
	 */
	public static List<Portfolio> retrieveAllPortfolios() {
		//Connecting to the DB.
		Connection conn = DBTool.connectToDB();
		String query  = "select * from Portfolio;";
				
		PreparedStatement ps = null;
		ResultSet rs = null;
		
		List<Portfolio> portfolios = new ArrayList<>();
		
		try {
			ps = conn.prepareStatement(query);
			rs = ps.executeQuery();
			Portfolio p = new Portfolio();
			/**
			 * The while loop functions to iterate through all portfolios. 
			 * Through each iteration, @retrievePerson and @retrieveAsset method is called and is passed accordingly 
			 * the person Id or the portfolio Id. The methods return the appropriate data needed to be fed into the constructor. 
			 * @retrievePerson returns a Person object and @retrieveAsset returns a list of assets. 
			 */
			while (rs.next()) {
				if (rs.getInt("benefId") >= 1) {
					p = new Portfolio(rs.getString("portCode"), 
							retrievePerson(rs.getInt("ownerId")), retrievePerson(rs.getInt("managerId")), retrievePerson(rs.getInt("benefId")), 
							retrieveAssets(rs.getInt("portId"))); 
				} else {
					p = new Portfolio(rs.getString("portCode"), 
							retrievePerson(rs.getInt("ownerId")), retrievePerson(rs.getInt("managerId")), retrieveAssets(rs.getInt("portId"))); 
				}
				
				portfolios.add(p);
			}
		}
			catch (SQLException sqle) {
				throw new RuntimeException(sqle);
			}
			DBTool.disconnectFromDB(conn, ps, rs); 
			return portfolios;
				
		}
}
