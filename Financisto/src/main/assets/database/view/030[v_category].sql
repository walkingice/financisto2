create view v_category AS 
SELECT
    _id as _id,
    title as title,
    left as left,
    right as right,
    type as type,
	last_project_id as last_project_id
FROM category
ORDER BY left;
	
