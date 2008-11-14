
def validN(value):
    try:
        return max(0, int(value))
    except ValueError:
        return 0

def validBool(value):
    return value in (True, 1, "1", "true", "on", "yes")

