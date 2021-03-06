package github.com.ioridazo.fundanalyzer.domain.domain.entity.transaction;

public enum Flag {
    ON("1"),
    OFF("0"),
    ;

    private final String flag;

    Flag(final String flag) {
        this.flag = flag;
    }

    public String toValue() {
        return flag;
    }

    @Override
    public String toString() {
        return "Flag{" +
                "flag='" + flag + '\'' +
                '}';
    }
}
