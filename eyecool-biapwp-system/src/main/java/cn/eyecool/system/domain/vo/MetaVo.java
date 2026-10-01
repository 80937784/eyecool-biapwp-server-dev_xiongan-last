package cn.eyecool.system.domain.vo;

/**
 * 路由显示信息
 * 
 * @author admin
 */
public class MetaVo {
    /**
     * 设置该路由在侧边栏和面包屑中展示的名字
     */
    private String title;

    /**
     * 设置该路由的图标，对应路径src/assets/icons/svg
     */
    private String icon;

    /**
     * 设置为true，则不会被 <keep-alive>缓存
     */
    private boolean noCache;

    /**
     * 是否是TAB页类型菜单
     */
    private boolean tabMenu;

    /**
     * 完整路由路径
     */
    private String fullPath;

    public MetaVo() {}

    public MetaVo(String title, String icon) {
        this.title = title;
        this.icon = icon;
    }

    public MetaVo(String title, String icon, boolean noCache) {
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
    }

    public MetaVo(String title, String icon, boolean noCache, boolean tabMenu, String fullPath) {
        super();
        this.title = title;
        this.icon = icon;
        this.noCache = noCache;
        this.tabMenu = tabMenu;
        this.fullPath = fullPath;
    }

    public boolean isNoCache() {
        return noCache;
    }

    public void setNoCache(boolean noCache) {
        this.noCache = noCache;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public boolean isTabMenu() {
        return tabMenu;
    }

    public void setTabMenu(boolean tabMenu) {
        this.tabMenu = tabMenu;
    }

    public String getFullPath() {
        return fullPath;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

}
