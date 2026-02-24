"""One-time script to create the transactions feature group in Hopsworks."""

import os

import hopsworks
from hsfs.feature import Feature

HOPSWORKS_HOST = os.environ["HOPSWORKS_HOST"]
HOPSWORKS_PORT = int(os.getenv("HOPSWORKS_PORT", "443"))
HOPSWORKS_PROJECT = os.environ["HOPSWORKS_PROJECT"]
HOPSWORKS_API_KEY = os.environ["HOPSWORKS_API_KEY"]
FEATURE_GROUP_NAME = os.getenv("FEATURE_GROUP_NAME", "transactions")
FEATURE_GROUP_VERSION = int(os.getenv("FEATURE_GROUP_VERSION", "1"))


def main():
    project = hopsworks.login(
        host=HOPSWORKS_HOST,
        port=HOPSWORKS_PORT,
        project=HOPSWORKS_PROJECT,
        api_key_value=HOPSWORKS_API_KEY,
    )
    fs = project.get_feature_store()

    features = [
        Feature(name="transaction_id", type="string"),
        Feature(name="event_time", type="bigint"),
        Feature(name="user_id", type="string"),
        Feature(name="amount", type="double"),
        Feature(name="currency", type="string"),
        Feature(name="category", type="string"),
    ]

    fg = fs.get_or_create_feature_group(
        name=FEATURE_GROUP_NAME,
        version=FEATURE_GROUP_VERSION,
        primary_key=["transaction_id"],
        event_time="event_time",
        online_enabled=True,
        stream=True,
        features=features,
    )

    if fg.id is None:
        fg.save()
        print(f"Feature group '{fg.name}' v{fg.version} created (id={fg.id})")
    else:
        print(f"Feature group '{fg.name}' v{fg.version} already exists (id={fg.id})")


if __name__ == "__main__":
    main()
