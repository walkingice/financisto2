create view v_category AS 
SELECT
    _id as _id,
    parent_id as parent_id,
    title as title,
    left as left,
    right as right,
    type as type,
	last_project_id as last_project_id,
    level as level
FROM category
ORDER BY left;
	
