/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbibackserver;

import com.alibaba.fastjson.JSON;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ray peng Sun
 */
public class CheckTrans {

    private static String JDBC_DRIVER = ReadDoc.getSqlInfo().get("JDBC_DRIVER").toString();
    private static String DB_URL = ReadDoc.getSqlInfo().get("DB_URL").toString();
    private static String USER = ReadDoc.getSqlInfo().get("USER").toString();
    private static String PASS = ReadDoc.getSqlInfo().get("PASS").toString();
    private static String CURRENCE = "BTC";
    private static String TABLE = "BTCTrans";

    public static void readTrans(String UID, String address) {

        String link = "https://api.blockcypher.com/v1/btc/test3/addrs/" + address + "/full";

        String s = Get.httpsRequest(link, "GET", null);
        HashMap result = JSON.parseObject(s, HashMap.class);
        List<String> txs = JSON.parseArray(result.get("txs").toString(), String.class);
        for (int i = 0; i < txs.size(); i++) {
            HashMap trade = JSON.parseObject(txs.get(i), HashMap.class);
            if (trade.get("block_height").toString().equals("-1")) {
                continue;
            }
            String hash = trade.get("hash").toString();
            double value = 0;
            List<String> outputs = JSON.parseArray(trade.get("outputs").toString(), String.class);
            for (int j = 0; j < outputs.size(); j++) {
                HashMap output = JSON.parseObject(outputs.get(j), HashMap.class);
                String outputAddress = JSON.parseArray(output.get("addresses").toString(), String.class).get(0);
                if (outputAddress.equals(address)) {
                    value = Double.parseDouble(output.get("value").toString()) / 100000000;
                }
            }

            if (!findTrade(hash)) {
                System.out.println(hash);
                System.out.println(address);
                System.out.println(value);
                addTrade(hash, address, value);
                update(UID, value);
            }
        }
    }

    public static boolean findTrade(String hash) {
        String sql = "Select * from Address." + TABLE + " where Hash = \"" + hash + "\";";

        try {
            Class.forName(JDBC_DRIVER);
            Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {

                rs.close();
                st.close();
                con.close();
                return true;
            }
            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return false;
    }

    public static void mapAddress() {
        String sql = "Select * from Address.BTCTestNet;";

        try {
            Class.forName(JDBC_DRIVER);
            Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            while (rs.next()) {

                String UID = rs.getString("UID");
                String address = rs.getString("Address");
                readTrans(UID, address);

            }
            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static void addTrade(String hash, String address, double value) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            String sql = "INSERT INTO Address." + TABLE + " (`Hash`, `Address`, `Value`) VALUES ('"
                    + hash + "', '" + address + "', '" + value + "');";
            boolean rs5 = stmt.execute(sql);

            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(SandbiBackServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void update(String UID, double amount) {
        try {
            Class.forName(JDBC_DRIVER);
            Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement st = con.createStatement();
            String sql = "update Sandbi.Balance set " + CURRENCE + " = " + (getBalance(UID) + amount) + " where UID=\"" + UID + "\";";
            st.execute(sql);
            st.close();
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public static double getBalance(String user) {
        String sql = "select * from `Balance` Where UID = '" + user + "';";
        double amount = 0;
        try {
            Class.forName(JDBC_DRIVER);
            Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                amount = Double.parseDouble(rs.getString(CURRENCE));
            }
            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return amount;
    }
}
