SELECT
  FPS.CLICKSEQ
  AS Sequence,
  FPS.NUMHITS
  AS Average_Hits,
  SDIM.SITEDESC,
  PDIM.DESCRIPTION
  AS Page,
  FPS.PATHID,
  PATHS.NUMHITS,
  PLS.NUMPATHS
  AS Recorded_Paths,
  PLS.NUMSESSIONS
  AS Total_Hits
FROM
  FULLPATHSTATS{timeSelector.queryGranularity} FPS,
  FULLPATHLENGTHSTATS{timeSelector.queryGranularity} PLS,
  PAGEDIM PDIM,
  SITEDIM SDIM,
  (SELECT
      SUBSELPATH.PATHID
      AS PATHID,
      MIN(SUBSELPATH.NUMHITS)
      AS NUMHITS,
      MAX(SUBSELPATH.CLICKSEQ)
      AS PATHLENGTH
    FROM
      FULLPATHSTATS{timeSelector.queryGranularity} SUBSELPATH,
      (SELECT
          VALIDPATHS.PATHID
          AS PATHID
        FROM
          FULLPATHSTATS{timeSelector.queryGranularity} VALIDPATHS
        WHERE
          (VALIDPATHS.STARTTIMESTAMP = {=timeSelector.queryStartDate}) AND
          ((VALIDPATHS.SITEID = {=siteSelector.selectedValue}) OR
          ({=siteSelector.selectedValue} = -1)) AND
          (VALIDPATHS.DELIVERYID = {=deliverySelector.selectedValue}) AND
          (VALIDPATHS.SEGMENTID = {=segmentSelector.selectedValue})
        GROUP BY
          VALIDPATHS.PATHID
      ) VALIDPATHS
    WHERE
      (VALIDPATHS.PATHID = SUBSELPATH.PATHID) AND
      (SUBSELPATH.STARTTIMESTAMP = {=timeSelector.queryStartDate})
    GROUP BY
      SUBSELPATH.PATHID
    HAVING
      (MAX(SUBSELPATH.CLICKSEQ) >= {=pathLength.threshold})
  ) PATHS
WHERE
  (PDIM.PAGEID = FPS.PAGEID) AND
  (SDIM.SITEID = FPS.SITEID) AND
  (PATHS.PATHID = FPS.PATHID) AND
  (FPS.STARTTIMESTAMP = {=timeSelector.queryStartDate}) AND
  (FPS.STARTTIMESTAMP = PLS.STARTTIMESTAMP) AND
  (PATHS.PATHLENGTH = PLS.LENGTH)
ORDER BY
  PATHS.NUMHITS DESC,
  FPS.PATHID ASC,
  FPS.NUMHITS DESC,
  FPS.CLICKSEQ ASC