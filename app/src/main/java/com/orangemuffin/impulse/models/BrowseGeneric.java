package com.orangemuffin.impulse.models;

import java.util.List;

/* Created by OrangeMuffin on 2018-12-15 */
public class BrowseGeneric {
    private String type;
    private String section_title;
    private List<GameInfo> mygames_list;
    private GameInfo gameInfo;

    public BrowseGeneric(String type) {
        this.type = type;
    }

    public int getType() {
        if (type.equals("section_title")) {
            return 0;
        } else if (type.equals("horizontal")) {
            return 1;
        } else if (type.equals("vertical")) {
            return 2;
        }
        return -1;
    }

    public String getSection_title() {
        return section_title;
    }

    public void setSection_title(String section_title) {
        this.section_title = section_title;
    }

    public List<GameInfo> getMygames_list() {
        return mygames_list;
    }

    public void setMygames_list(List<GameInfo> mygames_list) {
        this.mygames_list = mygames_list;
    }

    public GameInfo getGameInfo() {
        return gameInfo;
    }

    public void setGameInfo(GameInfo gameInfo) {
        this.gameInfo = gameInfo;
    }
}
