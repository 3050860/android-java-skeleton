package cs.netarium;

import android.app.Application;

import dev.b3nedikt.restring.Restring;
import dev.b3nedikt.restring.repository.MemoryStringsRepository;
import dev.b3nedikt.reword.RewordInterceptor;
import dev.b3nedikt.viewpump.ViewPump;

public class MyApplication extends Application {

    private String language = "es";

    @Override
    public void onCreate() {
        super.onCreate();

        Restring.init(this);
        Restring.stringRepository = new MemoryStringsRepository();
        ViewPump.init(RewordInterceptor.INSTANCE);

    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }
}
