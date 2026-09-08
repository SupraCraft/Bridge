package bridge.mvn;

public final class Dependency extends org.apache.maven.model.Dependency {

    // This method is called by maven when a string is encountered instead of a dependency expression.
    public void set(String value) {
        final String[] part;
        final int length;
        if ((length = (part = value.split("\\s*:+\\s*", 6)).length) < 3 || length > 5) {
            throw new IllegalArgumentException("Additional dependency format does not match groupId:artifactId:version[:type[:classifier]]");
        }
        setGroupId(part[0]);
        setArtifactId(part[1]);
        setVersion(part[2]);
        if (length > 3) setType(part[3]);
        if (length > 4) setClassifier(part[4]);
    }
}
