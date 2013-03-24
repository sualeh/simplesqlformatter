SELECT DISTINCT
 IMPS.STARTTIMESTAMP AS StartTime,
 TXNODEDIM.DESCRIPTION AS Description,
 IMPS.NODEID AS NodeID,
 IMPS.NUMIMPRESSIONS AS Impressions,
 IMPS.NUMSESSIONS AS Sessions,
 SEGMENTCOUNT.NUMVISITORS AS SegmentVisitors,
 ALLIMPS.NUMIMPRESSIONS AS AllVisitorSegmentImpressions,
 ALLVISITORSSEGMENTCOUNT.NUMVISITORS AS AllVisitorsSegmentVisitors,
 TOTALIMPS.NUMIMPRESSIONS AS TotalImpressions,
 SITESTATS.TOTALSESSIONS AS TotalSessions,
 PERIODCOUNT.PERIODCOUNT AS PeriodCount
FROM
 IMPRESSIONSTATS{timeSelector.queryGranularity} IMPS,
 TAXONOMYNODEDIM TXNODEDIM,
 IMPRESSIONSTATS{timeSelector.queryGranularity} ALLIMPS, 
 (
  SELECT
    SUM(stats.NUMIMPRESSIONS) AS NUMIMPRESSIONS
   FROM
    IMPRESSIONSTATS{timeSelector.queryGranularity} stats,
    TAXONOMYNODEDIM tnd
  WHERE
   stats.NODEID = tnd.NODEID AND
   stats.SEGMENTID = {=segmentSelector.selectedValue} AND
   stats.TAXONOMYID = 1 AND
   stats.DELIVERYID = {=deliverySelector.selectedValue} AND
   tnd.LEAFFLAG = 1 AND
   tnd.TAXONOMYID = 1 AND
   stats.SITEID = {=siteSelector.selectedValue} AND
   (stats.STARTTIMESTAMP BETWEEN {=timeSelector.queryStartDate} AND {=timeSelector.queryEndDate})
 ) TOTALIMPS,
 (
   SELECT
    sum(NUMSESSIONS) as TOTALSESSIONS
   FROM
    SITESTATS{timeSelector.queryGranularity}
  WHERE
   SEGMENTID = 0 AND   
   DELIVERYID = {=deliverySelector.selectedValue} AND   
   SITEID = {=siteSelector.selectedValue} AND (
   STARTTIMESTAMP BETWEEN {=timeSelector.queryStartDate} AND {=timeSelector.queryEndDate})
 ) SITESTATS,
 (
  SELECT
   AVG(NUMVISITORS) AS NUMVISITORS 
  FROM
   SEGMENTSTATS{timeSelector.queryGranularity}
  WHERE
   SEGMENTID = {=segmentSelector.selectedValue} AND (
   STARTTIMESTAMP BETWEEN {=timeSelector.queryStartDate} AND {=timeSelector.queryEndDate})
 ) SEGMENTCOUNT,
 (
  SELECT
   AVG(NUMVISITORS) AS NUMVISITORS 
  FROM
   SEGMENTSTATS{timeSelector.queryGranularity} 
  WHERE
   (SEGMENTID = 0) AND 
   (STARTTIMESTAMP BETWEEN {=timeSelector.queryStartDate} AND {=timeSelector.queryEndDate})
 ) ALLVISITORSSEGMENTCOUNT,
 (
  SELECT 
   COUNT(*) AS PERIODCOUNT
  FROM
   ANALYSISSEGMENTLIST
  WHERE
   ANALYSISID = 2 AND
   TIMEGRANULARITYID = {=timeSelector.queryGranularityID} AND
   COMPLETED = 1 AND
   STARTTIME BETWEEN {=timeSelector.queryStartDate} AND {=timeSelector.queryEndDate}
 ) PERIODCOUNT
WHERE
 (IMPS.TAXONOMYID = TXNODEDIM.TAXONOMYID) AND 
 (IMPS.NODEID = TXNODEDIM.NODEID) AND
 (ALLIMPS.NODEID = IMPS.NODEID) AND
 (ALLIMPS.STARTTIMESTAMP = IMPS.STARTTIMESTAMP) AND
 (ALLIMPS.SITEID = IMPS.SITEID) AND
 (ALLIMPS.DELIVERYID = IMPS.DELIVERYID) AND
 (ALLIMPS.SEGMENTID = 0) AND
 (ALLIMPS.TAXONOMYID = 1) AND
 (IMPS.STARTTIMESTAMP BETWEEN {=timeSelector.queryStartDate} AND {=timeSelector.queryEndDate}) AND
 (IMPS.SITEID = {=siteSelector.selectedValue}) AND
 (IMPS.DELIVERYID = {=deliverySelector.selectedValue}) AND
 (IMPS.SEGMENTID = {=segmentSelector.selectedValue}) AND
 (TXNODEDIM.LEAFFLAG = 1) AND
 (IMPS.TAXONOMYID = 1)
ORDER BY IMPS.STARTTIMESTAMP, IMPS.NUMIMPRESSIONS, TXNODEDIM.DESCRIPTION DESC
