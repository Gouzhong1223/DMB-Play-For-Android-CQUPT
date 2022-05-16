package cn.edu.cqupt.dmb.player.actives.leanback;

import java.util.ArrayList;
import java.util.List;

/**
 * @author qingsong
 */
public final class SceneTypeList {
    public static final String[] SCENE_TYPE_CATEGORY = {
            "视频", "轮播图", "音频", "安全信息", "课表"
    };

    private static List<SceneVO> list;
    private static long count = 0;

    public static List<SceneVO> getList() {
        if (list == null) {
//            list = setupMovies();
        }
        return new ArrayList<>();
    }

    public static List<SceneVO> setupMovies() {
        list = new ArrayList<>();
        String[] title = {
                "Zeitgeist 2010_ Year in Review",
                "Google Demo Slam_ 20ft Search",
                "Introducing Gmail Blue",
                "Introducing Google Fiber to the Pole",
                "Introducing Google Nose"
        };

        String description = "Fusce id nisi turpis. Praesent viverra bibendum semper. "
                + "Donec tristique, orci sed semper lacinia, quam erat rhoncus massa, non congue tellus est "
                + "quis tellus. Sed mollis orci venenatis quam scelerisque accumsan. Curabitur a massa sit "
                + "amet mi accumsan mollis sed et magna. Vivamus sed aliquam risus. Nulla eget dolor in elit "
                + "facilisis mattis. Ut aliquet luctus lacus. Phasellus nec commodo erat. Praesent tempus id "
                + "lectus ac scelerisque. Maecenas pretium cursus lectus id volutpat.";
        String[] studio = {
                "Studio Zero", "Studio One", "Studio Two", "Studio Three", "Studio Four"
        };
        String[] videoUrl = {
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review.mp4",
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search.mp4",
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue.mp4",
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole.mp4",
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose.mp4"
        };
        String[] bgImageUrl = {
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/bg.jpg",
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/bg.jpg",
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/bg.jpg",
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/bg.jpg",
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/bg.jpg",
        };
        String[] cardImageUrl = {
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Zeitgeist/Zeitgeist%202010_%20Year%20in%20Review/card.jpg",
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/Demo%20Slam/Google%20Demo%20Slam_%2020ft%20Search/card.jpg",
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Gmail%20Blue/card.jpg",
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Fiber%20to%20the%20Pole/card.jpg",
                "https://commondatastorage.googleapis.com/android-tv/Sample%20videos/April%20Fool's%202013/Introducing%20Google%20Nose/card.jpg"
        };

        for (int index = 0; index < title.length; ++index) {
            list.add(
                    buildMovieInfo(
                            title[index],
                            description,
                            studio[index],
                            videoUrl[index],
                            cardImageUrl[index],
                            bgImageUrl[index]));
        }

        return list;
    }

    private static SceneVO buildMovieInfo(
            String title,
            String description,
            String studio,
            String videoUrl,
            String cardImageUrl,
            String backgroundImageUrl) {
        SceneVO sceneVO = new SceneVO();
        sceneVO.setId(count++);
        sceneVO.setTitle(title);
        sceneVO.setDescription(description);
        sceneVO.setSubTitle(studio);
        sceneVO.setVideoUrl(videoUrl);
        return sceneVO;
    }
}
