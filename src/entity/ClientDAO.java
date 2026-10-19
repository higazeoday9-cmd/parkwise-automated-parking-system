package entity;

import java.sql.*;

public class ClientDAO {

    public Client getById(int clientID) throws SQLException {
        String sql = "SELECT * FROM Client WHERE clientID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, clientID);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    public Client getByPhone(String mobilePhone) throws SQLException {
        String sql = "SELECT * FROM Client WHERE mobilePhone=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, mobilePhone);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return map(rs);
            }
        }
    }

    /** Insert new client. If clientID=0 it will be generated as MAX+1. */
    public int insert(Client client) throws SQLException {
        if (client == null) throw new IllegalArgumentException("client is null");
        if (client.getMobilePhone() == null || client.getMobilePhone().isBlank())
            throw new IllegalArgumentException("mobilePhone is required");

        int id = client.getClientID();
        if (id <= 0) id = nextId();

        String sql = "INSERT INTO Client (clientID, firstName, lastName, mobilePhone) VALUES (?, ?, ?, ?)";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, client.getFirstName());
            ps.setString(3, client.getLastName());
            ps.setString(4, client.getMobilePhone());
            ps.executeUpdate();
        }
        client.setClientID(id);
        return id;
    }

    public void update(Client client) throws SQLException {
        if (client == null) throw new IllegalArgumentException("client is null");
        if (client.getClientID() <= 0) throw new IllegalArgumentException("clientID is required");

        String sql = "UPDATE Client SET firstName=?, lastName=?, mobilePhone=? WHERE clientID=?";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, client.getFirstName());
            ps.setString(2, client.getLastName());
            ps.setString(3, client.getMobilePhone());
            ps.setInt(4, client.getClientID());
            ps.executeUpdate();
        }
    }

    /** Upsert by phone (phone is your natural unique key for the UI). */
    public int upsertByPhone(String firstName, String lastName, String mobilePhone) throws SQLException {
        Client existing = getByPhone(mobilePhone);
        if (existing == null) {
            Client c = new Client();
            c.setFirstName(firstName);
            c.setLastName(lastName);
            c.setMobilePhone(mobilePhone);
            return insert(c);
        }
        existing.setFirstName(firstName);
        existing.setLastName(lastName);
        update(existing);
        return existing.getClientID();
    }

    private int nextId() throws SQLException {
        String sql = "SELECT MAX(clientID) FROM Client";
        try (Connection c = DB.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            int max = 0;
            if (rs.next()) {
                max = rs.getInt(1);
                if (rs.wasNull()) max = 0;
            }
            return max + 1;
        }
    }

    private Client map(ResultSet rs) throws SQLException {
        Client cl = new Client();
        cl.setClientID(rs.getInt("clientID"));
        cl.setFirstName(rs.getString("firstName"));
        cl.setLastName(rs.getString("lastName"));
        cl.setMobilePhone(rs.getString("mobilePhone"));
        return cl;
    }
}

