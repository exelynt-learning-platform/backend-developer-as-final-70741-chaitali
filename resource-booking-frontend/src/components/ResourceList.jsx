function ResourceList({
  resources,
  getResourceDetails,
  setSelectedResource,
}) {
  return (
    <div className="resource-list">
      {resources.map((resource) => (
        <div className="resource-card" key={resource.id}>
          <h2>{resource.name}</h2>

          <p>{resource.description}</p>

          <p>
            Status:{' '}
            {resource.available
              ? 'Available ✅'
              : 'Not Available ❌'}
          </p>

          <button onClick={() => getResourceDetails(resource.id)}>
            View Details
          </button>

          <button
            onClick={() => setSelectedResource(resource)}
            disabled={!resource.available}
          >
            Book Resource
          </button>
        </div>
      ))}
    </div>
  )
}

export default ResourceList