/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sandbibackserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.alibaba.fastjson.JSON;

public class ReadDoc {

    public static HashMap getSqlInfo() {
        String text = ReadFile("sql.json");
        HashMap result = JSON.parseObject(text, HashMap.class);
        return result;
    }

    public static String ReadFile(String path) {
        String text = "";
        try {
            File file = new File(path);

            FileReader fileReader = new FileReader(file);
            BufferedReader reader = new BufferedReader(fileReader);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + '\n');
            }
            text = sb.toString();
            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(ReadDoc.class.getName()).log(Level.SEVERE, null, ex);
        }
        return text;
    }
}
