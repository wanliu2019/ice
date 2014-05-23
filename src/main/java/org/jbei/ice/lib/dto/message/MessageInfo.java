package org.jbei.ice.lib.dto.message;

import java.util.ArrayList;
import java.util.Date;

import org.jbei.ice.lib.account.AccountTransfer;
import org.jbei.ice.lib.dao.IDataTransferModel;
import org.jbei.ice.lib.dto.group.UserGroup;

/**
 * DTO for message object
 *
 * @author Hector Plahar
 */
public class MessageInfo implements IDataTransferModel {

    private long id;
    private Date sent;
    private String from;
    private String message;
    private String title;
    private boolean read;
    private ArrayList<UserGroup> userGroups;
    private ArrayList<AccountTransfer> accounts;

    public MessageInfo() {
        userGroups = new ArrayList<UserGroup>();
        accounts = new ArrayList<AccountTransfer>();
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return this.id;
    }

    public Date getSent() {
        return sent;
    }

    public void setSent(Date sent) {
        this.sent = sent;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public ArrayList<UserGroup> getUserGroups() {
        return userGroups;
    }

    public ArrayList<AccountTransfer> getAccounts() {
        return accounts;
    }
}
