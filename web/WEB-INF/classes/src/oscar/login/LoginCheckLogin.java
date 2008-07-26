/*
 * Copyright (c) 2005. Department of Family Medicine, McMaster University. All Rights Reserved. *
 * This software is published under the GPL GNU General Public License. This program is free
 * software; you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version. * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU General Public License for more details. * * You should have
 * received a copy of the GNU General Public License along with this program; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA. * <OSCAR
 * TEAM> This software was written for the Department of Family Medicine McMaster Unviersity
 * Hamilton Ontario, Canada
 */
package oscar.login;

import java.sql.SQLException;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Vector;

import org.oscarehr.util.SpringUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.quatro.service.LookupManager;
import com.quatro.model.LookupCodeValue;
import java.util.List;

public class LoginCheckLogin {
    private LookupManager lookupManager = (LookupManager) SpringUtils.getBean("lookupManager");

    Properties pvar = null;

    LoginCheckLoginBean lb = null;

    LoginInfoBean linfo = null;

    LoginList llist = null;

    String propFileName = "";

    boolean propFileFound = true;

    public LoginCheckLogin() {
        setOscarVariable();
    }

    public boolean isBlock(String userId) {
        boolean bBlock = false;
        
        GregorianCalendar now = new GregorianCalendar();
        while (llist == null) {
            llist = LoginList.getLoginListInstance(); // LoginInfoBean info =
            // null;
        }
        String sTemp = null;

        // delete the old entry in the loginlist if time out
        if (!llist.isEmpty()) {
            for (Enumeration e = llist.keys(); e.hasMoreElements();) {
                sTemp = (String) e.nextElement();
                linfo = (LoginInfoBean) llist.get(sTemp);
                if (linfo.getTimeOutStatus(now))
                    llist.remove(sTemp);
            }

            // check if it is blocked
            if (llist.get(userId) != null && ((LoginInfoBean) llist.get(userId)).getStatus() == 0)
                bBlock = true;
        }
        return bBlock;
    }

    // authenticate is used to check password
    public String[] auth(String user_name, String password, String pin, String ip, ApplicationContext appContext) throws Exception, SQLException {
    	lb = new LoginCheckLoginBean();
        lb.ini(user_name, password, pin, ip, pvar);

        boolean isOk = false;
    	if ("yes".equals(oscar.OscarProperties.getInstance().getProperty("ldap_authentication")))
    	{
    		com.quatro.ldap.LdapAuthentication ldap = (com.quatro.ldap.LdapAuthentication) appContext.getBean("ldapAuthentication");
    		isOk = ldap.authenticate(user_name, password);
    	}
    	return lb.authenticate(isOk,appContext); 
    }
    
    // update login list if login failed
    public synchronized void updateLoginList(String userId) {
            GregorianCalendar now = new GregorianCalendar();
            if (llist.get(userId) == null) {
                linfo = new LoginInfoBean(now, Integer.parseInt(pvar.getProperty("login_max_failed_times")), Integer
                        .parseInt(pvar.getProperty("login_max_duration")));
            } else {
                linfo = (LoginInfoBean) llist.get(userId);
                linfo.updateLoginInfoBean(now, 1);
            }
            llist.put(userId, linfo);
            System.out.println(userId + "  status: " + ((LoginInfoBean) llist.get(userId)).getStatus() + " times: "
                    + linfo.getTimes() + " time: ");
    }

    // lock update login list if login failed
    public synchronized void updateLockList(String userId) {
            GregorianCalendar now = new GregorianCalendar();
            if (llist.get(userId) == null) {
                linfo = new LoginInfoBean(now, Integer.parseInt(pvar.getProperty("login_max_failed_times")), Integer
                        .parseInt(pvar.getProperty("login_max_duration")));
            } else {
                linfo = (LoginInfoBean) llist.get(userId);
                linfo.updateLoginInfoBean(now, 1);
            }
            llist.put(userId, linfo);
            System.out.println(userId + "  status: " + ((LoginInfoBean) llist.get(userId)).getStatus() + " times: "
                    + linfo.getTimes() + " time: ");
    }

    public void setOscarVariable() {
    	pvar = (Properties) oscar.OscarProperties.getInstance();
    	
    	List confKeyList = lookupManager.LoadCodeList("PRP", false, null,null);
    	for(int i=0; i<confKeyList.size(); i++)
    	{
    		LookupCodeValue ckv = (LookupCodeValue) confKeyList.get(i);
    		pvar.setProperty(ckv.getDescription(), ckv.getBuf1().toLowerCase());
    	}
    }

    public Properties getOscarVariable() {
        return pvar;
    }

    public String[] getPreferences() {
        return lb.getPreferences();
    }

    public boolean unlock(String userId) {
        boolean bBlock = false;

        while (llist == null) {
            llist = LoginList.getLoginListInstance();
        }
        String sTemp = null;

        // unlocl the entry in the loginlist
        if (!llist.isEmpty()) {
            for (Enumeration e = llist.keys(); e.hasMoreElements();) {
                sTemp = (String) e.nextElement();
                if (sTemp.equals(userId)) {
                    llist.remove(sTemp);
                    bBlock = true;
                }
            }
        }

        return bBlock;
    }

    public Vector findLockList() {
        Vector ret = new Vector();

        while (llist == null) {
            llist = LoginList.getLoginListInstance();
        }
        String sTemp = null;

        // unlocl the entry in the loginlist
        if (!llist.isEmpty()) {
            for (Enumeration e = llist.keys(); e.hasMoreElements();) {
                sTemp = (String) e.nextElement();
                ret.add(sTemp);
            }
        }

        return ret;
    }

	/**
	 * @return Returns the propFileFound.
	 */
	public boolean isPropFileFound() {
		return propFileFound;
	}

	/**
	 * @param propFileFound The propFileFound to set.
	 */
	public void setPropFileFound(boolean propFileFound) {
		this.propFileFound = propFileFound;
	}

	/**
	 * @return Returns the propFileName.
	 */
	public String getPropFileName() {
		return propFileName;
	}

	/**
	 * @param propFileName The propFileName to set.
	 */
	public void setPropFileName(String propFileName) {
		this.propFileName = propFileName;
	}
	
}
