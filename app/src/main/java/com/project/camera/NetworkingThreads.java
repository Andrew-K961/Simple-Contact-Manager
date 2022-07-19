package com.project.camera;

import android.content.res.AssetManager;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class NetworkingThreads {
    protected static final String APPLICATION_NAME = "Inventory App";
    protected static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    protected static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    protected static String spreadsheetId = "bruh";
    protected static NetHttpTransport HTTP_TRANSPORT;
    protected static DBHelper database;
    protected static AssetManager assets;
    protected static Sheets service;
    /*protected static String sheetTab = "Sheet1!";
    protected static int tabs = 1;*/

    public static void setVariables (AssetManager a, DBHelper db, String id){
        assets = a;
        database = db;
        spreadsheetId = id;
        //sheetTab = "Sheet" + tab + "!";
        //tabs = Integer.parseInt(tab);
    }

    protected static class Setup extends NotifyingThread {

        @Override
        public void doRun() {
            try {
                HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
                InputStream in = assets.open("credentials.json");
                GoogleCredentials googleCredentials = GoogleCredentials.fromStream(in).createScoped(SCOPES);

                service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, new HttpCredentialsAdapter(googleCredentials))
                        .setApplicationName(APPLICATION_NAME)
                        .build();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    protected static class UploadAll implements Runnable {

        @Override
        public void run() {
            try {
                ValueRange body = new ValueRange().setValues(database.getItemsUpload());
                service.spreadsheets().values().update(spreadsheetId, "A2", body).setValueInputOption("RAW").execute();
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    protected static class GetAllFromCloud extends NotifyingThread {

        @Override
        public void doRun() {
            try {
                ValueRange values = service.spreadsheets().values().get(spreadsheetId, "A2:H").execute();
                List<List<Object>> list = values.getValues();
                database.ReplaceAllItems(list);
               /* List<Object> list2 = new ArrayList<>();

                for (int i = 0; i < list.size(); i++){
                    list2.add(list.get(i).get(4));
                }
*/

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected static class AddRow extends NotifyingThread {
        public List<List<Object>> list;

        @Override
        public void doRun() {
            try {
                service.spreadsheets().values().append(spreadsheetId, "A2:H", new ValueRange().setValues(list))
                        .setValueInputOption("RAW").execute();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        AddRow (List<List<Object>> list){
            this.list = list;
        }
    }

    protected static class DeleteRow extends NotifyingThread {
        public int id;

        @Override
        public void doRun() {
            try {
                ValueRange values = service.spreadsheets().values().get(spreadsheetId, "A2:A").execute();
                List<List<Object>> list = values.getValues();
                int row;
                for (row = 0; row < list.size(); row++){
                    if (list.get(row).get(0).equals(String.valueOf(id))){
                        break;
                    }
                }
                BatchUpdateSpreadsheetRequest content = new BatchUpdateSpreadsheetRequest();
                Request request = new Request().setDeleteDimension(new DeleteDimensionRequest().setRange(
                        new DimensionRange().setSheetId(0).setDimension("ROWS").setStartIndex(row+1).setEndIndex(row+2)));
                content.setRequests(Collections.singletonList(request));
                service.spreadsheets().batchUpdate(spreadsheetId, content).execute();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        DeleteRow (int id){
            this.id = id;
        }
    }

    protected static class EditRow extends NotifyingThread {
        private final int id;

        @Override
        public void doRun() {
            try {
                ValueRange values = service.spreadsheets().values().get(spreadsheetId, "A2:A").execute();
                List<List<Object>> list = values.getValues();
                int row;
                for (row = 0; row < list.size(); row++){
                    if (list.get(row).get(0).equals(String.valueOf(id))){
                        break;
                    }
                }
                row += 2;
                ValueRange updated = new ValueRange().setValues(database.getRowForUpload(id));
                service.spreadsheets().values().update(spreadsheetId, "B"+row+":H"+row, updated).setValueInputOption("RAW").execute();
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        EditRow (int id){
            this.id = id;
        }
    }
}
