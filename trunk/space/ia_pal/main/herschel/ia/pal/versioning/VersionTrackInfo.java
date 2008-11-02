package herschel.ia.pal.versioning;

/**
 * Contains information of each component of an version track.
 * <p>
 * A string representation of an alias is of the form
 * </p>
 * <p>
 * basename[:version]
 * </p>
 * <p>
 * Where the version is optional
 * </p>
 * <p>
 * This class provides the basename, and if appropriate, the version number, of
 * the given string representation of an alias.
 * </p>
 * @deprecated Not needed any more after the refactoring of versioning.
 */
public class VersionTrackInfo {

	private boolean _isVersionSpecified = false;
	private String _trackId = null;
	private int _version = 0;

	public VersionTrackInfo(String trackId,
                            int version,
                            boolean isVersionSpecified) {
		_trackId = trackId;
		_version = version;
		_isVersionSpecified = isVersionSpecified;
	}

    public VersionTrackInfo(String trackId) {
        this(trackId, 0, false);
    }

    public VersionTrackInfo(String trackId, int version) {
        this(trackId, version, true);
    }

	public String getTrackId() {
		return _trackId;
	}

	public int getVersion() {
		return _version;
	}

	public boolean isVersionSpecified() {
		return _isVersionSpecified;
	}

	public String toString() {
		return _trackId + ":" + _version;
	}
}
