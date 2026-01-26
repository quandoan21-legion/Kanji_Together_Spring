package org.t2404e.kanji_together_db.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "notifications.failed-kanji")
public class FailedKanjiNotificationProperties {
    private boolean enabled = false;
    private String cron = "0 */5 * * * *";
    private int recentHours = 5;
    private int maxKanji = 10;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getRecentHours() {
        return recentHours;
    }

    public void setRecentHours(int recentHours) {
        this.recentHours = recentHours;
    }

    public int getMaxKanji() {
        return maxKanji;
    }

    public void setMaxKanji(int maxKanji) {
        this.maxKanji = maxKanji;
    }
}
