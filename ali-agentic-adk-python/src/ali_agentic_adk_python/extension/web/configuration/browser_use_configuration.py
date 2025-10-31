# Copyright (C) 2025 AIDC-AI
# This project incorporates components from the Open Source Software below.
# The original copyright notices and the licenses under which we received such components are set forth below for informational purposes.
#
# Open Source Software Licensed under the MIT License:
# --------------------------------------------------------------------
# 1. vscode-extension-updater-gitlab 3.0.1 https://www.npmjs.com/package/vscode-extension-updater-gitlab
# Copyright (c) Microsoft Corporation. All rights reserved.
# Copyright (c) 2015 David Owens II
# Copyright (c) Microsoft Corporation.
# Terms of the MIT:
# --------------------------------------------------------------------
# MIT License
#
# Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
#
# The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
#
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


import logging
from alibabacloud_ecd20200930.client import Client as ecd20200930Client
from alibabacloud_ecd20201002.client import Client as ecd20201002Client
from alibabacloud_appstream_center20210218.client import Client as appstreamcenter20210218Client
from alibabacloud_eds_aic20230930.client import Client as edsaic20230930Client
from src.ali_agentic_adk_python.extension.web.dto.browser_use_properties import BrowserUseProperties
from src.ali_agentic_adk_python.extension.web.utils.snowflake import StringBasedLetterSnowflake


logger = logging.getLogger(__name__)

class BrowserUseConfiguration:
    def __init__(self, browser_use_properties: BrowserUseProperties = None):
        self.browser_use_properties = browser_use_properties

    def client(self) -> ecd20200930Client | None:
        try:
            return self._create_client(self.browser_use_properties.endpoint)
        except Exception as e:
            logger.error("init alibabacloud_ecd20200930.Client error", exc_info=e)
            return None

    def client_jp(self) -> ecd20200930Client | None:
        try:
            return self._create_client("ecd.ap-northeast-1.aliyuncs.com")
        except Exception as e:
            logger.error("init alibabacloud_ecd20200930.Client error", exc_info=e)
            return None

    def client1002(self) -> ecd20201002Client | None:
        try:
            return self._create_client1002(self.browser_use_properties.endpoint)
        except Exception as e:
            logger.error("init alibabacloud_ecd20201002.Client error", exc_info=e)
            return None

    def app_stream_client(self) -> appstreamcenter20210218Client | None:
        if not getattr(self.browser_use_properties, "mobile_end_point", None):
            return None
        try:
            return self._create_app_stream_client(self.browser_use_properties.app_stream_end_point)
        except Exception as e:
            logger.error("init alibabacloud_appstream_center20210218.Client error", exc_info=e)
            return None

    def mobile_client(self) -> edsaic20230930Client | None:
        if not getattr(self.browser_use_properties, "app_stream_end_point", None):
            return None
        try:
            return self._create_mobile_client(self.browser_use_properties.mobile_end_point)
        except Exception as e:
            logger.error("init alibabacloud_eds_aic20230930.Client error", exc_info=e)
            return None

    def simplified_letter_snowflake(self) -> StringBasedLetterSnowflake | None:
        if not getattr(self.browser_use_properties, "computer_resource_id", None):
            return None
        return StringBasedLetterSnowflake(self.browser_use_properties.computer_resource_id)

    def atomic_operations(self):
        if not getattr(self.browser_use_properties, "enable", False):
            return None
        from src.ali_agentic_adk_python.extension.web.service.atomic_operations import AtomicOperations
        from src.ali_agentic_adk_python.extension.web.service.ecd import EcdService

        ecd_service = EcdService(self.browser_use_properties)
        return AtomicOperations(ecd_service, self.browser_use_properties)

    def app_builder_service(self):
        operations = self.atomic_operations()
        if operations is None:
            return None
        from src.ali_agentic_adk_python.extension.web.service.app_builder_service import AppBuilderService

        return AppBuilderService(operations)

    def _create_client(self, endpoint) -> ecd20200930Client:
        from alibabacloud_tea_openapi.models import Config
        config = Config(
            access_key_id=self.browser_use_properties.ak,
            access_key_secret=self.browser_use_properties.sk,
            endpoint=endpoint
        )
        return ecd20200930Client(config)

    def _create_client1002(self, endpoint) -> ecd20201002Client:
        from alibabacloud_tea_openapi.models import Config
        config = Config(
            access_key_id=self.browser_use_properties.ak,
            access_key_secret=self.browser_use_properties.sk,
            endpoint=endpoint
        )
        return ecd20201002Client(config)

    def _create_app_stream_client(self, endpoint) -> appstreamcenter20210218Client:
        from alibabacloud_tea_openapi.models import Config
        config = Config(
            access_key_id=self.browser_use_properties.ak,
            access_key_secret=self.browser_use_properties.sk,
            endpoint=endpoint
        )
        return appstreamcenter20210218Client(config)

    def _create_mobile_client(self, endpoint) -> edsaic20230930Client:
        from alibabacloud_tea_openapi.models import Config
        config = Config(
            access_key_id=self.browser_use_properties.ak,
            access_key_secret=self.browser_use_properties.sk,
            endpoint=endpoint
        )
        return edsaic20230930Client(config)
