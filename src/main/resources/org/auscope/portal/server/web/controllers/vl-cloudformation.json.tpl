{
  "AWSTemplateFormatVersion": "2010-09-09",
  "Metadata": {
    "AWS::CloudFormation::Designer": {
      "e0560b9a-0f01-4f0f-b8f9-6914d40d922c": {
        "size": {
          "width": 60,
          "height": 60
        },
        "position": {
          "x": 270,
          "y": 110
        },
        "z": 0,
        "embeds": [],
        "isassociatedwith": [
          "4eb02d3e-4a98-43fa-953c-ac0ab2114d3e"
        ]
      },
      "9d50e08c-5955-4bee-b1b4-77891f305c1a": {
        "source": {
          "id": "46ea9ac6-9838-436b-ad98-c81dff7992ef"
        },
        "target": {
          "id": "e0560b9a-0f01-4f0f-b8f9-6914d40d922c"
        },
        "z": 1
      },
      "f8a870e9-1ab3-4be2-a75b-407ab2a8f9ad": {
        "size": {
          "width": 60,
          "height": 60
        },
        "position": {
          "x": 310,
          "y": 190
        },
        "z": 0,
        "embeds": [],
        "isassociatedwith": [
          "7dd8e728-2bd1-401f-a0fb-94d304415d4e"
        ]
      },
      "98215be4-bacc-40fc-ac50-327117b3b1ef": {
        "source": {
          "id": "8d56cc5c-9650-4fc1-9d3e-92d7f1fb91e3"
        },
        "target": {
          "id": "f8a870e9-1ab3-4be2-a75b-407ab2a8f9ad"
        },
        "z": 11
      },
      "4eb02d3e-4a98-43fa-953c-ac0ab2114d3e": {
        "size": {
          "width": 60,
          "height": 60
        },
        "position": {
          "x": 130,
          "y": 110
        },
        "z": 0,
        "embeds": []
      },
      "5397347e-d4aa-4bcd-95af-a68e1c1155a2": {
        "source": {
          "id": "4eb02d3e-4a98-43fa-953c-ac0ab2114d3e"
        },
        "target": {
          "id": "e0560b9a-0f01-4f0f-b8f9-6914d40d922c"
        },
        "z": 11
      },
      "1588908d-a70c-41fc-9867-c965314844cf": {
        "source": {
          "id": "e0560b9a-0f01-4f0f-b8f9-6914d40d922c"
        },
        "target": {
          "id": "4eb02d3e-4a98-43fa-953c-ac0ab2114d3e"
        },
        "z": 12
      },
      "0800cdda-d910-4063-908f-f883147151a6": {
        "size": {
          "width": 60,
          "height": 60
        },
        "position": {
          "x": 400,
          "y": 110
        },
        "z": 0,
        "embeds": [],
        "isassociatedwith": [
          "e0560b9a-0f01-4f0f-b8f9-6914d40d922c"
        ]
      },
      "1cfb8483-d86b-4745-9a69-f2922b92f06a": {
        "source": {
          "id": "0800cdda-d910-4063-908f-f883147151a6"
        },
        "target": {
          "id": "e0560b9a-0f01-4f0f-b8f9-6914d40d922c"
        },
        "z": 11
      },
      "7dd8e728-2bd1-401f-a0fb-94d304415d4e": {
        "size": {
          "width": 60,
          "height": 60
        },
        "position": {
          "x": 130,
          "y": 190
        },
        "z": 0,
        "embeds": []
      },
      "47295e75-6f30-4212-a122-a94654b804e2": {
        "source": {
          "id": "f8a870e9-1ab3-4be2-a75b-407ab2a8f9ad"
        },
        "target": {
          "id": "7dd8e728-2bd1-401f-a0fb-94d304415d4e"
        },
        "z": 11
      },
      "5ea78b1b-2eab-4665-b6a1-dbbbcc8160ff": {
        "source": {
          "id": "e0560b9a-0f01-4f0f-b8f9-6914d40d922c"
        },
        "target": {
          "id": "4eb02d3e-4a98-43fa-953c-ac0ab2114d3e"
        },
        "z": 11
      }
    }
  },
  "Resources": {
    "AnvglStsRole": {
      "Type": "AWS::IAM::Role",
      "Properties": {
        "AssumeRolePolicyDocument": {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Effect": "Allow",
              "Principal": {
                "AWS": "arn:aws:iam::${awsAccount}"
              },
              "Action": "sts:AssumeRole",
              "Condition": {
                "StringEquals": {
                  "sts:ExternalId": "${awsSecret}"
                }
              }
            }
          ]
        },
        "ManagedPolicyArns": [
          {
            "Ref": "AnvglStsPolicy"
          }
        ]
      },
      "Metadata": {
        "AWS::CloudFormation::Designer": {
          "id": "f8a870e9-1ab3-4be2-a75b-407ab2a8f9ad"
        }
      }
    },
    "AnvglS3Policy": {
      "Type": "AWS::IAM::ManagedPolicy",
      "Properties": {
        "PolicyDocument": {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Sid": "Stmt1453960831000",
              "Effect": "Allow",
              "Action": [
                "s3:*"
              ],
              "Resource": [
                "arn:aws:s3:::${s3Bucket}*"
              ]
            }
          ]
        }
      },
      "Metadata": {
        "AWS::CloudFormation::Designer": {
          "id": "4eb02d3e-4a98-43fa-953c-ac0ab2114d3e"
        }
      }
    },
    "AnvglS3InstanceProfile": {
      "Type": "AWS::IAM::InstanceProfile",
      "Properties": {
        "Roles": [
          {
            "Ref": "AnvglS3Role"
          }
        ]
      },
      "Metadata": {
        "AWS::CloudFormation::Designer": {
          "id": "0800cdda-d910-4063-908f-f883147151a6"
        }
      }
    },
    "AnvglStsPolicy": {
      "Type": "AWS::IAM::ManagedPolicy",
      "Properties": {
        "PolicyDocument": {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Effect": "Allow",
              "Action": [
                "iam:PassRole"
              ],
              "Resource": [
                "arn:aws:iam::*:role/*-AnvglS3Role-*"
              ]
            },
            {
              "Effect": "Allow",
              "Action": [
                "ec2:CreateTags",
                "ec2:DeleteTags",
                "ec2:GetConsoleOutput",
                "ec2:ModifyInstanceAttribute"
              ],
              "Resource": [
                "*"
              ]
            },
            {
              "Effect": "Allow",
              "Action": [
                "ec2:RunInstances",
                "ec2:StartInstances",
                "ec2:StopInstances",
                "ec2:TerminateInstances"
              ],
              "Resource": [
                "arn:aws:ec2:*:*:image/*",
                "arn:aws:ec2:*:*:instance/*",
                "arn:aws:ec2:*:*:network-interface/*",
                "arn:aws:ec2:*:*:security-group/*",
                "arn:aws:ec2:*:*:key-pair/*",
                "arn:aws:ec2:*:*:subnet/*",
                "arn:aws:ec2:*:*:volume/*"
              ]
            },
            {
              "Effect": "Allow",
              "Action": [
                "s3:*"
              ],
              "Resource": [
                "arn:aws:s3:::${s3Bucket}*"
              ]
            }
          ]
        }
      },
      "Metadata": {
        "AWS::CloudFormation::Designer": {
          "id": "7dd8e728-2bd1-401f-a0fb-94d304415d4e"
        }
      }
    },
    "AnvglS3Role": {
      "Type": "AWS::IAM::Role",
      "Properties": {
        "AssumeRolePolicyDocument": {
          "Version": "2012-10-17",
          "Statement": [
            {
              "Effect": "Allow",
              "Principal": {
                "Service": "ec2.amazonaws.com"
              },
              "Action": "sts:AssumeRole"
            }
          ]
        },
        "ManagedPolicyArns": [
          {
            "Ref": "AnvglS3Policy"
          }
        ]
      },
      "Metadata": {
        "AWS::CloudFormation::Designer": {
          "id": "e0560b9a-0f01-4f0f-b8f9-6914d40d922c"
        }
      }
    }
  }
}