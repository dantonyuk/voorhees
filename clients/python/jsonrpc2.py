import requests
import json


class JsonRpcError(Exception):
    def __init__(self, message, error):
        super().__init__(message)
        self.error = error


class HttpError(Exception):
    def __init__(self, message, status):
        super().__init__(message)
        self.status = status


class JsonRpcClient:
    def __init__(self, url, *args, **kwargs):
        self._url = url
        self._req_args = args
        self._req_kwargs = kwargs

    def _post(self, payload):
        if (self._req_kwargs.get("headers")):
            self._req_kwargs["headers"]["Content-Type"] = "application/json;charset=utf-8"
        response = requests.post(self._url, data=json.dumps(payload), *self._req_args, **self._req_kwargs)
        response.raise_for_status()
        if response.status_code == 200:
            resp_json = response.json()
            error = resp_json.get("error")
            if error:
                raise JsonRpcError(error)
            return DictAdapter.create(resp_json.get("result"))
        else:
            return None

    def _curl(self, payload):
        headers = self._req_kwargs.get("headers", {})
        headers["Content-Type"] = "application/json;charset=utf-8"
        headers = ' \\\n'.join(["-H '%s:%s'" % (k,v) for k, v in headers.items()])
        return "curl -X POST \\\n%s \\\n-d '%s' \\\n'%s'" % (headers, json.dumps(payload), self._url)

    def _httpie(self, payload):
        return "http '%s' \\\n<<< '%s'" % (self._url, json.dumps(payload))

    def batch(self):
        return JsonRpcBatch(self)

    def __getattr__(self, attr):
        return JsonRpcMethod(self, attr)

class JsonRpcMethod:
    def __init__(self, client, method):
        self._client = client
        self._method = method

    def __call__(self, *args, **kwargs):
        return self._client._post(__json_request(self._method, __params(*args, **kwargs), id=1)

    def notify(*args, **kwargs):
        return self._client._post(__json_request(self._method, __params(*args, **kwargs)))

    def curl(self, *args, **kwargs):
        return self._client._curl(__json_request(self._method, __params(*args, **kwargs), id=1))

    def httpie(self, *args, **kwargs):
        return self._client._httpie(__json_request(self._method, __params(*args, **kwargs), id=1))


class JsonRpcBatch:
    def __init__(self, client):
        self._client = client
        self.batch = []
        self.seq = 0

    def call(self, method, params):
        self.seq += 1
        self.batch.append(__json_request(methods, params, self.seq))
        return self

    def notify(self, method, params):
        self.batch.append(__json_request(methods, params))
        return self

    def __getattr__(self, attr):
        return JsonRpcBatchMethod(self, attr)

    def post(self):
        return self._client._post(self.batch)

    def curl(self, *args, **kwargs):
        return self._client._curl(self.batch)

    def httpie(self, *args, **kwargs):
        return self._client._httpie(self.batch)


class JsonRpcBatchMethod:
    def __init__(self, batch, method):
        self._batch = batch
        self._method = method

    def __call__(self, *args, **kwargs):
        return self._batch.call(self._method, __params(*args, **kwargs))

    def notify(self, *args, **kwargs):
        return self._batch.notify(self._method, __params(*args, **kwargs))


def __params(*args, **kwargs):
    return args and args or kwargs


def __json_request(method, params=None, id=None):
    return {
        "id": id,
        "jsonrpc": "2.0",
        "method": method,
        "params": params
    }


class DictAdapter:
    def __init__(self, obj):
        self.obj = obj

    @staticmethod
    def create(obj):
        if type(obj) in [dict, list]:
            return DictAdapter(obj)
        else:
            return obj

    def __getitem__(self, attr):
        return DictAdapter.create(self.obj[attr])

    def __getattr__(self, attr):
        return DictAdapter.create(self.obj[attr])

    def __iter__(self):
        return iter(self.obj)

    def __len__(self):
        return len(self.obj)

    def __str__(self):
        return str(self.obj)

    def __repr__(self):
        return repr(self.obj)
