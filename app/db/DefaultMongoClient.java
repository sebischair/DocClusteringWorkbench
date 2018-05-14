package db;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;
import play.Configuration;

import java.util.Arrays;

public class DefaultMongoClient {
    static public Morphia morphia;
    static public Datastore datastore;

    static public void connect() throws Exception{
        String dockerHost = "mongo";    // For docker, don't provide credentials to database
        Configuration configuration = Configuration.root();
        String dbUrl = configuration.getString("morphia.db.url");
        String dbName = configuration.getString("morphia.db.name");
        int dbPort = configuration.getInt("morphia.db.port");
        boolean isAuthEnabled = configuration.getBoolean("morphia.db.isAuthEnabled");

        ServerAddress sa = new ServerAddress(dbUrl, dbPort);

        morphia = new Morphia();
        morphia.mapPackage("app.model");
        if (dbUrl.equals(dockerHost) || !isAuthEnabled) {
            datastore = morphia.createDatastore(new MongoClient(sa), dbName);
        } else {
            String userName = configuration.getString("morphia.db.username");
            String password = configuration.getString("morphia.db.pwd");
            MongoCredential credential = MongoCredential.createCredential(userName, dbName, password.toCharArray());
            datastore = morphia.createDatastore(new MongoClient(sa, Arrays.asList(credential)), dbName);
        }
        datastore.ensureIndexes();
        datastore.ensureCaps();
    }
}


