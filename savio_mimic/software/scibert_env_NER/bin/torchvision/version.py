__version__ = '0.4.0'
git_version = 'ef3ba78bdafe6a3bc56c48bedd9d95f02063c3a8'
from torchvision import _C
if hasattr(_C, 'CUDA_VERSION'):
    cuda = _C.CUDA_VERSION
