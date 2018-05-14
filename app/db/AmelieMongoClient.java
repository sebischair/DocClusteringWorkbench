package db;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoDatabase;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import play.Configuration;

import java.util.Arrays;

public class AmelieMongoClient {
    static public Morphia amelieMorphia;
    static public Datastore amelieDataStore;
    static public MongoDatabase amelieDatabase;


    static public void connect() throws Exception {
        String dockerHost = "mongo";    // For docker, don't provide credentials to database
        Configuration configuration = Configuration.root();
        String dbUrl = configuration.getString("morphia.db.url");
        String dbName = configuration.getString("morphia.amelie.db.name");
        int dbPort = configuration.getInt("morphia.db.port");
        boolean isAuthEnabled = configuration.getBoolean("morphia.db.isAuthEnabled");
        MongoClient mongoClient;
        ServerAddress sa = new ServerAddress(dbUrl, dbPort);
        if (dbUrl.equals(dockerHost) || !isAuthEnabled) {
            mongoClient = new MongoClient(sa);
        } else {
            String userName = configuration.getString("morphia.db.username");
            String password = configuration.getString("morphia.db.pwd");
            MongoCredential credential = MongoCredential.createCredential(userName, dbName, password.toCharArray());
            mongoClient = new MongoClient(sa, Arrays.asList(credential));
        }

        amelieMorphia = new Morphia();
        amelieMorphia.mapPackage("app.model.amelie");
        amelieDataStore = amelieMorphia.createDatastore(mongoClient, dbName);
        amelieDatabase = mongoClient.getDatabase(dbName);

        amelieDataStore.ensureIndexes();
        amelieDataStore.ensureCaps();
    }
}


