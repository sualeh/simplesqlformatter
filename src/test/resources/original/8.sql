SELECT RSTATS.STARTTIMESTAMP AS Start_Time,
DECODE (STATSREFERRALDIM.DESCRIPTION, NULL, 'No Referrer', STATSREFERRALDIM.DESCRIPTION) AS Referral_URL,
STATSREFERRALDIM.NODEID AS Referral_ID,
RSTATS.NUMSESSIONS AS Segment_Sessions,
PERIODCOUNT.PERIODCOUNT AS Period_Count,
SSTATS.NUMSESSIONS AS Total_Sessions,
RSTATS.NUMVISITORS AS Referral_NumVisitors, 
SEGMENTCOUNT.NUMVISITORS AS Segment_Count,
RSTATS2.NUMVISITORS AS Referral_AllVisitors,
ALLVISITORSSEGMENTCOUNT.NUMVISITORS AS AllVisitors_Segment_Count,
RSTATS.NUMPAGES AS Total_pages,
RSTATS.NUMIMPRESSIONS AS Impressions,
(RSTATS.TOTALTIMESPENT)/60 AS Minutes
FROM REFERRALSTATS{timeSelector.queryGranularity} RSTATS, 
  REFERRALSTATS{timeSelector.queryGranularity} RSTATS2,  
  (select (SITEDESCR.DESCRIPTION || RDD1.DESCRIPTION) DESCRIPTION, 
          RND1.NODEID NODEID
   from REFERRALNODEDIM RND1,REFERRALDESCRIPTIONDIM RDD1, REFERRALTAXONOMY RT,  
        REFERRALNODEDIM SITETABLE, 
        REFERRALDESCRIPTIONDIM SITEDESCR
   where RDD1.DESCID=RND1.DESCID and 
         RND1.TYPEID=2 and 
         RND1.NODEID=RT.NODEID and          
         RT.PARENTNODEID=SITETABLE.NODEID and 
         SITETABLE.TYPEID=1 and 
         SITETABLE.DESCID=SITEDESCR.DESCID 
) STATSREFERRALDIM,
(
   SELECT AVG (NUMVISITORS) AS NUMVISITORS 
   FROM SEGMENTSTATS{timeSelector.queryGranularity} 
   WHERE SEGMENTID = {=segmentSelector.selectedValue}  AND 
         (STARTTIMESTAMP BETWEEN {=timeSelector.queryStartDate} AND {=timeSelector.queryEndDate})
) SEGMENTCOUNT,  
(
   SELECT AVG (NUMVISITORS) AS NUMVISITORS 
   FROM SEGMENTSTATS{timeSelector.queryGranularity} 
   WHERE SEGMENTID = 0 AND 
         (STARTTIMESTAMP BETWEEN {=timeSelector.queryStartDate} AND {=timeSelector.queryEndDate})
) ALLVISITORSSEGMENTCOUNT,   
(
   SELECT SUM(NUMSESSIONS) AS NUMSESSIONS 
   FROM V_SITESTATS{timeSelector.queryGranularity} SS
   WHERE (SS.STARTTIMESTAMP BETWEEN {=timeSelector.queryStartDate} AND {=timeSelector.queryEndDate}) AND
   (SS.SITEID = {=siteSelector.selectedValue}) AND
   (SS.DELIVERYID = {=deliverySelector.selectedValue}) AND
   (SS.SEGMENTID = {=segmentSelector.selectedValue})
) SSTATS,  
(
   SELECT COUNT(*) AS PERIODCOUNT
   FROM ANALYSISSEGMENTLIST 
   WHERE ANALYSISID = 4 AND 
   TIMEGRANULARITYID = {=timeSelector.queryGranularityID} AND 
   COMPLETED = 1 AND
   STARTTIME BETWEEN {=timeSelector.queryStartDate} AND {=timeSelector.queryEndDate}
) PERIODCOUNT, 
 (select NODEID from 
   (select RS.NODEID,SUM(RS.NUMSESSIONSSTARTED) CRIT from REFERRALSTATS{timeSelector.queryGranularity} RS, REFERRALNODEDIM RDIM 
    where RDIM.TYPEID=2 and 
    RDIM.NODEID=RS.NODEID and
    (RS.STARTTIMESTAMP BETWEEN {=timeSelector.queryStartDate} AND {=timeSelector.queryEndDate}) AND
    (RS.SITEID = {=siteSelector.selectedValue}) AND
    (RS.DELIVERYID = {=deliverySelector.selectedValue}) AND
    (RS.SEGMENTID = {=segmentSelector.selectedValue})
    group by RS.NODEID
    order by CRIT desc
   ) LIMITER_BABY  
   where rownum <= {maxDBRows.max}
  ) LIMITER 
WHERE 
(RSTATS2.NODEID=LIMITER.NODEID) AND 
(RSTATS.NODEID = STATSREFERRALDIM.NODEID) AND
(RSTATS2.SEGMENTID = 0) AND
(RSTATS2.SITEID = RSTATS.SITEID) AND
(RSTATS2.DELIVERYID = RSTATS.DELIVERYID) AND
(RSTATS2.STARTTIMESTAMP = RSTATS.STARTTIMESTAMP) AND
(RSTATS2.NODEID = RSTATS.NODEID) AND 
(RSTATS.STARTTIMESTAMP BETWEEN {=timeSelector.queryStartDate} AND {=timeSelector.queryEndDate}) AND 
(RSTATS.SITEID = {=siteSelector.selectedValue}) AND
(RSTATS.DELIVERYID = {=deliverySelector.selectedValue}) AND
(RSTATS.SEGMENTID = {=segmentSelector.selectedValue})
ORDER BY Referral_URL, Start_Time
