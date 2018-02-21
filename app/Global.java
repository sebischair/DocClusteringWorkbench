import db.AmelieMongoClient;
import db.DefaultMongoClient;
import play.Application;
import play.GlobalSettings;
import play.Logger;

/**
 * Created by Manoj on 10/24/2016.
 */
public class Global extends GlobalSettings {

    @Override
    public void onStart(play.Application arg0) {
        super.beforeStart(arg0);
        Logger.debug("** onStart **");
        try {
            DefaultMongoClient.connect();
            AmelieMongoClient.connect();
            Logger.debug("** Connected to datastore: " + DefaultMongoClient.datastore.getDB() + "and " + AmelieMongoClient.amelieDataStore.getDB());
        } catch (Exception e) {
            Logger.error("** Cannot connect to mongo: " + e.toString());
        }
    }

    public void onStop(Application app) {
        Logger.info("Application shutdown...");
    }
}