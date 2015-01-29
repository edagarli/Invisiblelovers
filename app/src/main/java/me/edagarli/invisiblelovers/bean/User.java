package me.edagarli.invisiblelovers.bean;


import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobRelation;

/**
 * Created by jhp-android on 14-12-9.
 */
public class User extends BmobUser{

    public static final String TAG = "User";

    private String signature;
    /**
     * //性别-true-男
     */
    private boolean sex;

    private String niceName;

    private BmobFile avatar;
    private BmobRelation favorite;

    /**
     * //-true-lover
     */
    private boolean status;


    public BmobRelation getFavorite() {
        return favorite;
    }

    public void setFavorite(BmobRelation favorite) {
        this.favorite = favorite;
    }

    public BmobFile getAvatar() {
        return avatar;
    }

    public void setAvatar(BmobFile avatar) {
        this.avatar = avatar;
    }

    public boolean isSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    public String getNiceName() {
        return niceName;
    }

    public void setNiceName(String niceName) {
        this.niceName = niceName;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }


    private Integer count;


    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }



}
