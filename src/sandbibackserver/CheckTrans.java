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

    public static void readTrans() {

        String link = "https://algoindexer.testnet.algoexplorerapi.io/v2/accounts/" + "42OFZZPR45B3XZDBFLXX62TIXNSKYPCJACOSE3MR72JFI3F6Q7NC2GJSCQ" + "/transactions";

        String s = Get.httpsRequest(link, "GET", null);
        HashMap result = JSON.parseObject(s, HashMap.class);
        List<String> txs = JSON.parseArray(result.get("transactions").toString(), String.class);
        for (int i = 0; i < txs.size(); i++) {

            HashMap trade = JSON.parseObject(txs.get(i), HashMap.class);
            String hash;
            int assetid;
            double value;
            String senderAddress;
            if (trade.containsKey("asset-transfer-transaction")) {
                hash = trade.get("id").toString();
                HashMap asset = JSON.parseObject(trade.get("asset-transfer-transaction").toString(), HashMap.class);
                value = Double.parseDouble(asset.get("amount").toString());
                assetid = Integer.parseInt(asset.get("asset-id").toString());
                senderAddress = trade.get("sender").toString();
                
            } else {
                hash = trade.get("id").toString();
                HashMap asset = JSON.parseObject(trade.get("payment-transaction").toString(), HashMap.class);
                value = Double.parseDouble(asset.get("amount").toString());
                assetid = -1;
                senderAddress = trade.get("sender").toString();
            }
            String UID = getUID(senderAddress);
            if (!findTrade(hash, assetid)) {
                System.out.println(hash);
                System.out.println(senderAddress);
                System.out.println(value);
                addTrade(hash, senderAddress, value, assetid);
                if(UID != null){
                    update(UID, value, assetid);
                }
                
            }
        }
    }
    
    public static void main(String[] args){
        System.out.println(getAssetName(-1));
    }
    
    public static String getAssetName(int assetid){
        String sql = "Select * from Address.Asset where AssetID = \"" + assetid + "\";";

        try {
            Class.forName(JDBC_DRIVER);
            Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {
                String AssetName = rs.getString("AssetName");
                rs.close();
                st.close();
                con.close();
                return AssetName;
            }
            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            Logger.getLogger(ReadDoc.class.getName()).log(Level.SEVERE, null, e);
        }
        return null;
    }

    public static boolean findTrade(String hash, int assetid) {
        String sql = "Select * from Address." + getAssetName(assetid) + "Trans" + " where Hash = \"" + hash + "\";";

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
            Logger.getLogger(ReadDoc.class.getName()).log(Level.SEVERE, null, e);
        }
        return false;
    }

    public static void addTrade(String hash, String address, double value, int assetid) {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement stmt = conn.createStatement();
            String sql = "INSERT INTO Address." + getAssetName(assetid) + "Trans" + " (`Hash`, `Address`, `Value`) VALUES ('"
                    + hash + "', '" + address + "', '" + value + "');";
            boolean rs5 = stmt.execute(sql);

            stmt.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(SandbiBackServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void update(String UID, double amount, int assetid) {
        try {
            Class.forName(JDBC_DRIVER);
            Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement st = con.createStatement();
            String sql = "update Sandbi.Balance set " + getAssetName(assetid) + " = " + (getBalance(UID, assetid) + amount) + " where UID=\"" + UID + "\";";
            st.execute(sql);
            st.close();
            con.close();
        } catch (Exception e) {
            Logger.getLogger(ReadDoc.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static double getBalance(String user, int assetid) {
        String sql = "select * from `Balance` Where UID = '" + user + "';";
        double amount = 0;
        try {
            Class.forName(JDBC_DRIVER);
            Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);
            while (rs.next()) {
                amount = Double.parseDouble(rs.getString(getAssetName(assetid)));
            }
            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            Logger.getLogger(ReadDoc.class.getName()).log(Level.SEVERE, null, e);
        }
        return amount;
    }
    public static String getUID(String Address) {
        String sql = "Select * from Address.ALGOAddress Where Address = \"" + Address + "\";";
        String UID = null;
        try {
            Class.forName(JDBC_DRIVER);
            Connection con = DriverManager.getConnection(DB_URL, USER, PASS);
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery(sql);

            if (rs.next()) {

                UID = rs.getString("UID");

            }
            rs.close();
            st.close();
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        return UID;
    }
}
