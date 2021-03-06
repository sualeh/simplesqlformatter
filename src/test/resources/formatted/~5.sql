/* Get the 2000 pages most likely to exhibit affinity on the selected site, ordered alphabetically */
SELECT
  PAGEID{affinityDirection},
  DESCRIPTION
FROM
  (SELECT
      TS.PAGEID{affinityDirection},
      PD.DESCRIPTION,
      MAX((CAST(TS.NUMHITSAB
      AS DECIMAL) / CAST(TS.NUMHITSA
      AS DECIMAL) / CAST(TS.NUMHITSB
      AS DECIMAL))) PAGERANK
    FROM
      TEMPORALSTATSDAILY TS,
      (SELECT
          PAGEID,
          SITEID,
          DESCRIPTION
        FROM
          PAGEDIM
        WHERE
          (SITEID = {=siteID}) AND
          (DESCRIPTION IS NOT
          NULL)
      ) PD
    WHERE
      (PD.PAGEID = TS.PAGEID{affinityDirection}) AND
      (PD.SITEID = TS.SITEID{affinityDirection}) AND
      (TS.DELIVERYID = -1) AND
      (TS.SEGMENTID = 0)
    GROUP BY
      TS.PAGEID{affinityDirection},
      PD.DESCRIPTION
    ORDER BY
      PAGERANK DESC
  )
WHERE
  ROWNUM <= 2000
ORDER BY
  DESCRIPTION