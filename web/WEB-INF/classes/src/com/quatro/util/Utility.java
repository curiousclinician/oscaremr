package com.quatro.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.SimpleTimeZone;
import java.util.TimeZone;

import com.quatro.common.KeyConstants;

public class Utility {
    public static boolean IsEmpty(String pStr)
    {
        if (pStr == null || pStr.trim().equals(""))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    public static boolean IsDate(String pStr)
    {
    	java.sql.Date date = oscar.MyDateFormat.getSysDate(pStr);
    	return date != null;
    }
    public static boolean IsInt(String pStr)
    {
    	boolean isInt = true;
    	try 
    	{
    		Integer i = Integer.valueOf(pStr);
    		isInt = (i != null);
    	}
    	catch (Exception e)
    	{
    		isInt = false;
    	}
    	return isInt;
    }
    public static boolean IsIntBiggerThanZero(String pStr)
    {
    	boolean isInt = true;
    	try 
    	{
    		Integer i = Integer.valueOf(pStr);
    		isInt = (i != null);
    		if(i.intValue()<0) isInt=false;
    	}
    	catch (Exception e)
    	{
    		isInt = false;
    	}
    	return isInt;
    }
    // Convert dd/mm/yyyy d/m/yyyy to System format
    public static Date GetSysDate(String pDate) // throws Exception
    {
        Date date;
        String sTemp1, sTemp2;
        int day = 31;
        int month = 12;
        int year = 2999;
     
        if (IsEmpty(pDate)){
          try{
           	  return SetDate(year, month, day);
          }catch(Exception ex){ return null;}    
        }
        
        String delim = "/";

        if ("TODAY".equals(pDate.toUpperCase()))
        {
            return new Date();
        }
        else
        {
            try
            {
                int idx = pDate.indexOf(delim);
                day = Integer.parseInt(pDate.substring(0, idx));

                sTemp1 = pDate.substring(idx + 1);
                idx = sTemp1.indexOf(delim);
                month = Integer.parseInt(sTemp1.substring(0, idx))-1;

                sTemp2 = sTemp1.substring(idx + 1);
                idx = sTemp2.indexOf(delim);
                year = Integer.parseInt(sTemp2.substring(0, 4));

                date = SetDate(year, month, day);

                return date;
            }
            catch(Exception ex)
            {
            	return null;
//                throw ex; //new Exception("Invalid Date - the input date is in wrong format or out of range");
            }
        }
    }
    
    public static Date SetDate(int year, int month, int day) throws Exception
    {
	   Calendar c1 = Calendar.getInstance();
	   c1.set(year, month , day);
	   return c1.getTime();
    }
    // Convert dd/mm/yyyy to System format
    public static Date GetSysDateMin(String pDate) throws Exception
    {
        if (IsEmpty(pDate)){
			Calendar c1 = Calendar.getInstance();
			c1.set(1900, 1 , 1);
			return c1.getTime();
        }

        if ("TODAY".equals(pDate.toUpperCase())) return new Date();

        try
        {
            int day = Integer.parseInt(pDate.substring(0, 2));
            int month = Integer.parseInt(pDate.substring(3, 2))-1;
            int year = Integer.parseInt(pDate.substring(6, 4));

            Calendar c2 = Calendar.getInstance();
			c2.set(year, month, day);
			return c2.getTime();
        }
        catch(Exception ex)
        {
            throw(new Exception("Invalid Date - the input date is in wrong format or out of range"));
        }
    }
 
    public static Date GetSysDateMax(String pDate) throws Exception
    {
        if (IsEmpty(pDate)){
			return SetDate(2999, 12, 31);
        }
        
        if ("TODAY".equals(pDate.toUpperCase())) return new Date();
        
        try
        {
            int day = Integer.parseInt(pDate.substring(0, 2));
            int month = Integer.parseInt(pDate.substring(3, 2))-1;
            int year = Integer.parseInt(pDate.substring(6, 4));
			return SetDate(year, month, day);
        }
        catch(Exception ex)
        {
            throw(new Exception("Invalid Date - the input date is in wrong format or out of range"));
        }
    }
    
    // Convert a date to dd/mm/yyyy format
    public static String FormatDate(Date pDate) //throws Exception
    {
        try{
    	if(pDate==null) return "";
//    	if (pDate.getYear() < 1) pDate = Utility.SetDate(1,1,1);
        if (pDate.equals(Utility.SetDate(1,1,1))) return "";
        else{
//            return pDate.ToString("dd/MM/yyyy", System.Globalization.DateTimeFormatInfo.InvariantInfo);
   		   SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
   		   return formatter.format(pDate);
        }
        }catch(Exception ex){
        	return null;
        }
    }
    public static String getMergedClientQueryString(String tableAlias,String colName ){
    	if(IsEmpty(colName)) return "";
    	String sql=" or ";
    	if(!IsEmpty(tableAlias))sql+=tableAlias+"."; 
        sql+=colName +" in (select cm.clientId from ClientMerge cm where cm.deleted=false and cm.mergedToClientId=?)" ;    	
    	return sql;    	
    }
    public static String getUserOrgQueryString(String providerNo,Integer shelterId){
    	String progSQL="";
    	if (shelterId  == null || shelterId.intValue() == 0) {
			progSQL = "(select p.id from Program p where 'P' || p.id in (select a.code from LstOrgcd a, Secuserrole b " +
		       " where a.fullcode like '%' || b.orgcd || '%' and b.providerNo='" + providerNo + "'))";				
		}	else {
			progSQL = "(select p.id from Program p where p.shelterId =" + shelterId.toString() + " and 'P' || p.id in (select a.code from LstOrgcd a, Secuserrole b " +
		       " where a.fullcode like '%' || b.orgcd || '%' and b.providerNo='" + providerNo + "'))";				
		}
    	return progSQL;
    }
    public static String getUserOrgSqlString(String providerNo,Integer shelterId){
    	String progSQL="";
    	if (shelterId == null || shelterId.intValue() == 0) {
			progSQL = "(select p.program_id from program p where 'P' || p.program_id in (select a.code from lst_orgcd a, secuserrole b " +
		       " where a.fullcode like '%' || b.orgcd || '%' and b.provider_no='" + providerNo + "'))";				
		}	else {
			progSQL = "(select p.program_id from program p where p.shelter_id =" + shelterId.toString() + " and 'P' || p.program_id in (select a.code from lst_orgcd a, secuserrole b " +
		       " where a.fullcode like '%' || b.orgcd || '%' and b.provider_no='" + providerNo + "'))";				
		}
    	return progSQL;
    }
    public static String FormatDate(Date pDate, String fStr) //throws Exception
    {
        try{
    	if(pDate==null) return "";
//    	if (pDate.getYear() < 1) pDate = Utility.SetDate(1,1,1);
        if (pDate.equals(Utility.SetDate(1,1,1))) return "";
        else if(IsEmpty(fStr)==false){
           //yyyyMMdd
   		   SimpleDateFormat formatter = new SimpleDateFormat(fStr);
   		   return formatter.format(pDate);
        }
        else{
   		   SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
   		   return formatter.format(pDate);
        }
        }catch(Exception ex){
        	return null;
        }
    }
    public static String replace(String str, String pattern, String replaceTo)
    {
    	String[] buff = str.split(pattern);
    	StringBuffer sb = new StringBuffer();
    	
    	sb.append(buff[0]);
    	for(int i=1; i<buff.length;i++)
    	{
    		sb.append(replaceTo);
    		sb.append(buff[i]);
    	}
    	return sb.toString();
    }
    public static String merge(String[] str, String sep)
    {
    	if (str == null || str.length ==0) return "";
    	StringBuffer sb = new StringBuffer();
    	sb.append(str[0]);
    	for(int i=1; i<str.length;i++)
    	{
    		sb.append(sep);
    		sb.append(str[i]);
    	}
    	return sb.toString();
    }
    
    private static boolean checkIfNowAtDST(String iTimezoneName){ 
       	boolean isDST = false;
       	TimeZone iTimezone = TimeZone.getTimeZone(iTimezoneName);
        SimpleTimeZone stz = new SimpleTimeZone(iTimezone.getRawOffset(),
       		iTimezoneName, Calendar.MARCH, 8, -Calendar.SUNDAY,
            7200000, Calendar.NOVEMBER, 1, -Calendar.SUNDAY,
            7200000, 3600000); 
        if(stz.inDaylightTime(new Date())){
          	isDST = true;
        } 

        return isDST;
      } 

}
