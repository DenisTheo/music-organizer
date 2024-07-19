// Fetch parameters
var pathsParam = url.templateArgs.paths;
var paths = []

if (pathsParam)
	paths = pathsParam.split(',');

// Check Paths
if (paths.length === 0)
{
    status.code = 400;
    status.message = "No path provided.";
    status.redirect = true;
}

// FreeMarker Model Creation
model.paths = paths;
