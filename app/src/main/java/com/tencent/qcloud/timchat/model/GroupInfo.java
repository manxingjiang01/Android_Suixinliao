package com.tencent.qcloud.timchat.model;

import android.content.Context;
import android.content.Intent;

import com.tencent.TIMGroupDetailInfo;
import com.tencent.TIMGroupMemberRoleType;
import com.tencent.qcloud.presentation.event.GroupEvent;
import com.tencent.qcloud.presentation.presenter.GroupManagerPresenter;
import com.tencent.qcloud.presentation.viewfeatures.GroupInfoView;
import com.tencent.qcloud.timchat.MyApplication;
import com.tencent.qcloud.timchat.R;
import com.tencent.qcloud.timchat.ui.GroupProfileActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

/**
 * 群数据结构
 */
public class GroupInfo implements GroupInfoView,Observer {


    private Map<String, List<GroupProfile>> groups;
    private GroupManagerPresenter presenter;
    public static final String publicGroup = "Public", privateGroup = "Private", chatRoom = "ChatRoom";

    private GroupInfo(){
        groups = new HashMap<>();
        groups.put(publicGroup,new ArrayList<GroupProfile>());
        groups.put(privateGroup, new ArrayList<GroupProfile>());
        groups.put(chatRoom, new ArrayList<GroupProfile>());
        //注册群关系监听
        GroupEvent.getInstance().addObserver(this);
        presenter = new GroupManagerPresenter(this);
        if (GroupEvent.getInstance().isInit()){
            presenter.getGroupList();
        }
    }

    private static GroupInfo instance = new GroupInfo();

    public static GroupInfo getInstance(){
        return instance;
    }

    /**
     * 显示群资料
     *
     * @param groupInfos 群资料信息列表
     */
    @Override
    public void showGroupInfo(List<TIMGroupDetailInfo> groupInfos) {
        for (TIMGroupDetailInfo item : groupInfos){
            groups.get(item.getGroupType()).add(new GroupProfile(item));
        }
    }

    /**
     * This method is called if the specified {@code Observable} object's
     * {@code notifyObservers} method is called (because the {@code Observable}
     * object has been updated.
     *
     * @param observable the {@link Observable} object.
     * @param data       the data passed to {@link Observable#notifyObservers(Object)}.
     */
    @Override
    public void update(Observable observable, Object data) {
        if (observable instanceof GroupEvent){
            if (data == null){
                presenter.getGroupList();
            }else{
                if (data instanceof TIMGroupDetailInfo){
                    updateGroup((TIMGroupDetailInfo) data);
                }else if (data instanceof String){
                    delGroup((String) data);
                }
            }
        }
    }

    private void updateGroup(TIMGroupDetailInfo info){
        for (GroupProfile item : groups.get(info.getGroupType())){
            if (item.getIdentify().equals(info.getGroupId())){
                item.setProfile(info);
                return;
            }
        }
        groups.get(info.getGroupType()).add(new GroupProfile(info));
    }

    private void delGroup(String id){
        for (String key : groups.keySet()){
            Iterator<GroupProfile> iterator = groups.get(key).iterator();
            while(iterator.hasNext()){
                GroupProfile item = iterator.next();
                if (item.getIdentify().equals(id)){
                    iterator.remove();
                    return;
                }
            }
        }
    }

    /**
     * 是否在群内
     *
     * @param id 群identify
     */
    public boolean isInGroup(String id){
        for (String key : groups.keySet()){
            for (GroupProfile item : groups.get(key)){
                if (item.getIdentify().equals(id)) return true;
            }
        }
        return false;
    }

    /**
     * 按照群类型获取群
     *
     * @param type 群类型
     */
    public List<ProfileSummary> getGroupListByType(String type){
        List<ProfileSummary> result = new ArrayList<>();
        result.addAll(groups.get(type));
        return result;
    }


    public static String getTypeName(String type){
        if (type.equals(GroupInfo.publicGroup)){
            return MyApplication.getContext().getString(R.string.public_group);
        }else if (type.equals(GroupInfo.privateGroup)){
            return MyApplication.getContext().getString(R.string.discuss_group);
        }else if (type.equals(GroupInfo.chatRoom)){
            return MyApplication.getContext().getString(R.string.chatroom);
        }
        return "";
    }

}