package com.fidelity.leap.paysprint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionSearchDao {

    private Connection connection;

    public TransactionSearchDao(Connection connection) {
        this.connection = connection;
    }

    // FIX (A05 — Injection / SQL Injection):
    // Replaced Statement + string concatenation with PreparedStatement and a
    // positional parameter (?). The JDBC driver sends the query and the parameter
    // value separately — the database treats the parameter as data, never as SQL
    // syntax, regardless of what characters it contains. An input of
    // "' OR '1'='1" is searched for literally, not interpreted as SQL.
    public List<Transaction> searchByMerchant(String merchantName) throws SQLException {
        String sql = "SELECT id, merchant_name, amount FROM transactions WHERE merchant_name = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, merchantName);
            try (ResultSet rs = stmt.executeQuery()) {
                return mapResults(rs);
            }
        }
    }

    private List<Transaction> mapResults(ResultSet rs) throws SQLException {
        List<Transaction> results = new ArrayList<>();
        while (rs.next()) {
            results.add(new Transaction(
                    rs.getLong("id"),
                    rs.getString("merchant_name"),
                    rs.getDouble("amount")));
        }
        return results;
    }
}
